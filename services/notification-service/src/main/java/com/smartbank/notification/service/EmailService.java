package com.smartbank.notification.service;
import com.smartbank.notification.entity.Notification;
import com.smartbank.notification.event.StatementRequestedEvent;
import com.smartbank.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate;

    @Value("${notification.default-recipient}")
    private String defaultRecipient;

    @Value("${notification.from-address}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.notificationRepository = notificationRepository;
        this.restTemplate = new RestTemplate();
    }

    // =========================================================
    // Generic email sender
    // =========================================================

    public void sendEmail(String eventType, String templateName,
                          String subject, Map<String, Object> variables) {
        sendEmailWithPriority(eventType, templateName, subject, false, variables);
    }

    public void sendEmailWithPriority(String eventType, String templateName,
                                      String subject, boolean urgent,
                                      Map<String, Object> variables) {

        String finalSubject = urgent ? "[URGENT] " + subject : subject;

        Notification notification = new Notification();
        notification.setEventType(eventType);
        notification.setChannel("EMAIL");
        notification.setRecipient(defaultRecipient);
        notification.setSubject(finalSubject);
        notification.setStatus(Notification.NotificationStatus.PENDING);

        try {
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("urgent", urgent);

            String htmlBody = templateEngine.process("emails/" + templateName, context);
            notification.setBody(htmlBody);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(defaultRecipient);
            helper.setSubject(finalSubject);
            helper.setText(htmlBody, true);
            mailSender.send(message);

            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info(" Email sent: [{}] to {}", finalSubject, defaultRecipient);

        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setFailureReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            log.error(" Failed to send email: {}", e.getMessage());
            throw new RuntimeException("Email send failed", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    // =========================================================
    // Statement email (PDF attachment)
    // =========================================================

    public void sendStatementEmail(StatementRequestedEvent event) {
        Notification notification = new Notification();
        notification.setEventType("STATEMENT_REQUESTED");
        notification.setChannel("EMAIL");
        notification.setRecipient(event.getRecipientEmail());
        notification.setSubject("Your account statement - " + event.getAccountNumber());
        notification.setStatus(Notification.NotificationStatus.PENDING);

        try {
            // 1. Fetch PDF from account-service via internal Docker network
            String pdfUrl = "http://account-service:8080/api/v1/accounts/"
                    + event.getAccountId() + "/statement?from="
                    + event.getFromDate() + "&to=" + event.getToDate();

            byte[] pdfBytes = restTemplate.getForObject(pdfUrl, byte[].class);

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new RuntimeException("Empty PDF received from account-service");
            }

            log.info(" Fetched PDF ({} bytes) for account {}",
                    pdfBytes.length, event.getAccountNumber());

            // 2. Render HTML body
            Context ctx = new Context();
            ctx.setVariable("accountNumber", event.getAccountNumber());
            ctx.setVariable("fromDate", event.getFromDate().toString());
            ctx.setVariable("toDate", event.getToDate().toString());
            ctx.setVariable("generatedAt", LocalDateTime.now().toString());

            String htmlBody = templateEngine.process("emails/statement", ctx);
            notification.setBody(htmlBody);

            // 3. Build email with attachment
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(event.getRecipientEmail());
            helper.setSubject("Your account statement - " + event.getAccountNumber());
            helper.setText(htmlBody, true);
            helper.addAttachment(
                    "statement-" + event.getAccountNumber() + ".pdf",
                    new ByteArrayResource(pdfBytes)
            );

            mailSender.send(message);

            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info(" Statement email sent to {}", event.getRecipientEmail());

        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setFailureReason(truncate(e.getMessage(), 500));
            notification.setRetryCount(notification.getRetryCount() + 1);
            log.error(" Failed to send statement email: {}", e.getMessage(), e);
            throw new RuntimeException("Statement email failed", e);
        } finally {
            notificationRepository.save(notification);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
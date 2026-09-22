package com.smartbank.notification.service;
import com.smartbank.notification.entity.Notification;
import com.smartbank.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
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
    }
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
}
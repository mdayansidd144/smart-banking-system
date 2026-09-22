import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
public class TestSmtp {
    public static void main(String[] args) throws Exception {
        String user = "ayansiddiqui1411@gmail.com";
        String pass = "xlfpzywpsaahxznt";
        Properties p = new Properties();
        p.put("mail.smtp.host", "smtp.gmail.com");
        p.put("mail.smtp.port", "587");
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(p, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(user));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(user));
        msg.setSubject("SMTP Test from Smart Bank");
        msg.setText("If you see this, your Gmail credentials work!");
        Transport.send(msg);
        System.out.println(" SUCCESS: Email sent!");
    }
}
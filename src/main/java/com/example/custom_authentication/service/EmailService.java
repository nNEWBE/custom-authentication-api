package com.example.custom_authentication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String baseUrl;

    // Constructor injection (recommended by blog)
    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.from:noreply@example.com}") String fromEmail,
            @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.baseUrl = baseUrl;
    }

    /**
     * Send a plain text email (for simple notifications)
     */
    public void sendPlainText(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Plain text email sent to {}", to);
        } catch (MailException e) {
            log.error("Failed to send plain text email to {}: {}", to, e.getMessage());
            throw e; // Re-throw so caller can handle
        }
    }

    /**
     * Send an HTML email (for rich formatted emails)
     */
    public void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml
            mailSender.send(message);
            log.info("HTML email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to create email message for {}: {}", to, e.getMessage());
            throw e;
        } catch (MailException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new MessagingException("Failed to send email", e);
        }
    }

    /**
     * Send verification email to newly registered users
     */
    public void sendVerificationEmail(String to, String token) {
        String verificationLink = baseUrl + "/api/auth/verify?token=" + token;

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .button:hover { background: #5a6fd6; }
                        .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                        .link { word-break: break-all; color: #667eea; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome! 🎉</h1>
                        </div>
                        <div class="content">
                            <p>Thank you for registering with us!</p>
                            <p>Please click the button below to verify your email address:</p>
                            <center>
                                <a href="%s" class="button">Verify My Account</a>
                            </center>
                            <p>Or copy and paste this link into your browser:</p>
                            <p class="link">%s</p>
                            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                            <p><strong>⏰ This link will expire in 10 minutes.</strong></p>
                            <p>If you didn't create an account, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 Custom Authentication Demo</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(verificationLink, verificationLink);

        try {
            sendHtml(to, "Verify Your Email Address", htmlBody);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            log.warn("Email sending failed - please check your SMTP configuration.");
            log.info("DEVELOPMENT MODE - Verification link: {}", verificationLink);
        }
    }
}

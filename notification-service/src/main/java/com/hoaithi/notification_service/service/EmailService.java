package com.hoaithi.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@yourdomain.com}")
    private String fromEmail;

    /**
     * Gửi email chào mừng người dùng mới
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to Video Social Networking Platform! 🎉";
        String htmlContent = buildWelcomeEmailTemplate(username);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Gửi OTP để reset password
     */
    public void sendOtpEmail(String to, String otp) {
        String subject = "Password Reset Request - Your OTP Code";
        String htmlContent = buildOtpEmailTemplate(otp);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Phương thức chung để gửi HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Template email chào mừng
     */
    private String buildWelcomeEmailTemplate(String username) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table role="presentation" style="width: 100%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 40px 40px 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 10px 10px 0 0; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: bold;">
                                            🎬 Welcome Aboard!
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <h2 style="margin: 0 0 20px; color: #333333; font-size: 24px;">
                                            Hi {{USERNAME}}! 👋
                                        </h2>
                                        <p style="margin: 0 0 15px; color: #666666; font-size: 16px; line-height: 1.6;">
                                            Thank you for joining <strong>Video Social Networking Platform</strong>! We're excited to have you as part of our creative community.
                                        </p>
                                        <p style="margin: 0 0 25px; color: #666666; font-size: 16px; line-height: 1.6;">
                                            You can now start sharing your amazing videos, connect with other creators, and explore endless content.
                                        </p>
                                        
                                        <!-- CTA Button -->
                                        <table role="presentation" style="margin: 30px 0;">
                                            <tr>
                                                <td align="center">
                                                    <a href="https://yourplatform.com/explore" style="display: inline-block; padding: 14px 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 16px;">
                                                        Start Exploring
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Features -->
                                        <div style="margin-top: 30px; padding-top: 30px; border-top: 1px solid #eeeeee;">
                                            <h3 style="margin: 0 0 20px; color: #333333; font-size: 18px;">What you can do:</h3>
                                            <table role="presentation" style="width: 100%;">
                                                <tr>
                                                    <td style="padding: 10px 0;">
                                                        <span style="color: #667eea; font-size: 20px; margin-right: 10px;">📹</span>
                                                        <span style="color: #666666; font-size: 15px;">Upload and share your videos</span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 10px 0;">
                                                        <span style="color: #667eea; font-size: 20px; margin-right: 10px;">💬</span>
                                                        <span style="color: #666666; font-size: 15px;">Engage with the community</span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 10px 0;">
                                                        <span style="color: #667eea; font-size: 20px; margin-right: 10px;">🔔</span>
                                                        <span style="color: #666666; font-size: 15px;">Follow your favorite creators</span>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 10px 10px; text-align: center;">
                                        <p style="margin: 0 0 10px; color: #999999; font-size: 14px;">
                                            Need help? Contact us at <a href="mailto:support@yourplatform.com" style="color: #667eea; text-decoration: none;">support@yourplatform.com</a>
                                        </p>
                                        <p style="margin: 0; color: #999999; font-size: 12px;">
                                            © 2024 Video Social Networking Platform. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;

        return template.replace("{{USERNAME}}", username != null ? username : "there");
    }

    /**
     * Template email OTP
     */
    private String buildOtpEmailTemplate(String otp) {
        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table role="presentation" style="width: 100%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 40px 40px 20px; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); border-radius: 10px 10px 0 0; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: bold;">
                                            🔐 Password Reset
                                        </h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 40px;">
                                        <p style="margin: 0 0 20px; color: #666666; font-size: 16px; line-height: 1.6;">
                                            We received a request to reset your password. Use the verification code below to proceed:
                                        </p>
                                        
                                        <!-- OTP Box -->
                                        <table role="presentation" style="margin: 30px auto; width: 100%;">
                                            <tr>
                                                <td align="center" style="padding: 20px; background-color: #f8f9fa; border-radius: 10px; border: 2px dashed #e0e0e0;">
                                                    <div style="font-size: 36px; font-weight: bold; color: #f5576c; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                                                        {{OTP_CODE}}
                                                    </div>
                                                    <p style="margin: 10px 0 0; color: #999999; font-size: 14px;">
                                                        Valid for 10 minutes
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Warning -->
                                        <div style="margin-top: 30px; padding: 20px; background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 5px;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ⚠️ <strong>Security Notice:</strong><br>
                                                If you didn't request this password reset, please ignore this email or contact our support team immediately.
                                            </p>
                                        </div>
                                        
                                        <p style="margin: 25px 0 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                            For your security, never share this code with anyone.
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 10px 10px; text-align: center;">
                                        <p style="margin: 0 0 10px; color: #999999; font-size: 14px;">
                                            Need help? Contact us at <a href="mailto:support@yourplatform.com" style="color: #f5576c; text-decoration: none;">support@yourplatform.com</a>
                                        </p>
                                        <p style="margin: 0; color: #999999; font-size: 12px;">
                                            © 2024 Video Social Networking Platform. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;

        return template.replace("{{OTP_CODE}}", otp);
    }
}
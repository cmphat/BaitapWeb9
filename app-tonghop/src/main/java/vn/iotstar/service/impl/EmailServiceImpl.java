package vn.iotstar.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static volatile String lastSentOtp;

    public static String getLastSentOtp() {
        return lastSentOtp;
    }

    private final JavaMailSender mailSender;

    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp, String subject, String messageType) {
        lastSentOtp = otp;
        String content = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;">
                <h2 style="color: #4338ca; text-align: center;">MÃ XÁC THỰC OTP - BÀI TẬP WEB 9</h2>
                <p>Xin chào,</p>
                <p>Bạn vừa thực hiện yêu cầu <strong>%s</strong> trong hệ thống.</p>
                <div style="background-color: #f1f5f9; padding: 15px; border-radius: 8px; text-align: center; margin: 20px 0;">
                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #1e293b;">%s</span>
                </div>
                <p style="color: #64748b; font-size: 14px;">Mã OTP có hiệu lực trong vòng <strong>5 phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>
                <p style="color: #94a3b8; font-size: 12px; border-top: 1px solid #e2e8f0; padding-top: 10px; margin-top: 20px;">
                    Email này được gửi tự động từ hệ thống Spring Boot Security - ThS. Nguyễn Hữu Trung.
                </p>
            </div>
            """.formatted(messageType, otp);

        // Always log OTP prominently to console for testing/development
        logger.info("\n" +
                "======================================================================\n" +
                "[THÔNG BÁO OTP EMAIL CHO TEST / MÔI TRƯỜNG CỤC BỘ]\n" +
                "Gửi tới email : {}\n" +
                "Mục đích      : {}\n" +
                "MÃ OTP CỦA BẠN: {}\n" +
                "Thời hạn      : 5 phút\n" +
                "======================================================================",
                toEmail, messageType, otp);

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(content, true);
                mailSender.send(message);
                logger.info("Đã gửi email OTP thực tế qua SMTP tới {}", toEmail);
            } catch (Exception e) {
                logger.warn("Không thể gửi email qua SMTP (Do chưa cấu hình Gmail App Password hợp lệ). OTP đã được ghi log ở trên: {}", e.getMessage());
            }
        }
    }
}

package vn.iotstar.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp, String subject, String messageType);
}

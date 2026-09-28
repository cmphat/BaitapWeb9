package vn.iotstar.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpServiceImpl(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    private String hashOtp(String rawOtp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawOtp.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi thuật toán mã hóa SHA-256", e);
        }
    }

    @Override
    @Transactional
    public String generateAndSendOtp(String email, OtpType type) {
        // Kiểm tra giới hạn gửi lại (cooldown 60s)
        Optional<OtpToken> latestToken = otpTokenRepository.findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(email, type);
        if (latestToken.isPresent()) {
            OtpToken token = latestToken.get();
            long secondsSinceLast = Duration.between(token.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLast < 60) {
                long waitTime = 60 - secondsSinceLast;
                throw new IllegalStateException("Vui lòng đợi " + waitTime + " giây nữa trước khi yêu cầu gửi lại OTP!");
            }
            // Vô hiệu hóa token cũ
            token.setUsed(true);
            otpTokenRepository.save(token);
        }

        // Tạo mã 6 chữ số ngẫu nhiên
        int randomCode = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(randomCode);

        // Lưu vào CSDL với mã băm SHA-256 (không lưu plaintext)
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpHash(hashOtp(rawOtp))
                .otpType(type)
                .expiryTime(LocalDateTime.now().plusMinutes(5)) // Hết hạn sau 5 phút
                .attempts(0)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpTokenRepository.save(otpToken);

        // Gửi email
        String subject = type == OtpType.REGISTRATION
                ? "Mã xác thực kích hoạt tài khoản - Bài tập Web 9"
                : "Mã xác thực đặt lại mật khẩu - Bài tập Web 9";

        String messageType = type == OtpType.REGISTRATION
                ? "Đăng ký kích hoạt tài khoản"
                : "Đặt lại mật khẩu";

        emailService.sendOtpEmail(email, rawOtp, subject, messageType);

        return rawOtp;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String rawOtp, OtpType type) {
        OtpToken token = otpTokenRepository.findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(email, type)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mã OTP hoặc mã đã được sử dụng."));

        // Kiểm tra hết hạn
        if (token.isExpired()) {
            token.setUsed(true);
            otpTokenRepository.save(token);
            throw new IllegalArgumentException("Mã OTP đã hết hạn (quá 5 phút). Vui lòng yêu cầu gửi lại mã mới!");
        }

        // Kiểm tra số lần nhập sai (tối đa 5 lần)
        if (token.getAttempts() >= 5) {
            token.setUsed(true);
            otpTokenRepository.save(token);
            throw new IllegalArgumentException("Bạn đã nhập sai mã OTP quá 5 lần. Mã này đã bị khóa!");
        }

        // Kiểm tra tính đúng đắn của mã băm
        String hashedInput = hashOtp(rawOtp);
        if (!token.getOtpHash().equals(hashedInput)) {
            token.setAttempts(token.getAttempts() + 1);
            otpTokenRepository.save(token);
            int remaining = 5 - token.getAttempts();
            throw new IllegalArgumentException("Mã OTP không chính xác. Bạn còn " + remaining + " lần thử!");
        }

        // Xác thực thành công: đánh dấu used = true để không thể tái sử dụng
        token.setUsed(true);
        otpTokenRepository.save(token);
        return true;
    }

    @Override
    @Transactional
    public void resendOtp(String email, OtpType type) {
        generateAndSendOtp(email, type);
    }
}

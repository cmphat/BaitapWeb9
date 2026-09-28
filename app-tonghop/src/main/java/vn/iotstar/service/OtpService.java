package vn.iotstar.service;

import vn.iotstar.entity.OtpType;

public interface OtpService {

    String generateAndSendOtp(String email, OtpType type);

    boolean verifyOtp(String email, String rawOtp, OtpType type);

    void resendOtp(String email, OtpType type);
}

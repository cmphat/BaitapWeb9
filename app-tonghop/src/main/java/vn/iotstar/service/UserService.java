package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.*;

public interface UserService {

    void register(RegisterDTO registerDTO);

    void verifyRegisterOtp(VerifyOtpDTO verifyOtpDTO);

    void resendRegisterOtp(String email);

    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    void resendResetOtp(String email);

    Page<UserDTO> findUsers(String keyword, Pageable pageable);

    UserDTO getUserById(Long id);

    void createUserByAdmin(UserDTO userDTO, String rawPassword);

    void updateUserByAdmin(Long id, UserDTO userDTO);

    void toggleUserStatus(Long id);

    void deleteUser(Long id);

    long getTotalUserCount();
}

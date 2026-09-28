package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.*;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.UserService;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserMapper userMapper,
                           OtpService otpService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }

        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + registerDTO.getUsername() + "' đã có người sử dụng!");
        }

        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + registerDTO.getEmail() + "' đã được đăng ký trong hệ thống!");
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        User user = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .enabled(false) // Chưa kích hoạt tài khoản
                .role(roleUser)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Tạo và gửi OTP xác thực qua email
        otpService.generateAndSendOtp(user.getEmail(), OtpType.REGISTRATION);
    }

    @Override
    @Transactional
    public void verifyRegisterOtp(VerifyOtpDTO verifyOtpDTO) {
        User user = userRepository.findByEmail(verifyOtpDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + verifyOtpDTO.getEmail()));

        if (user.isEnabled()) {
            throw new IllegalStateException("Tài khoản này đã được kích hoạt trước đó. Vui lòng đăng nhập!");
        }

        // Xác thực OTP
        otpService.verifyOtp(verifyOtpDTO.getEmail(), verifyOtpDTO.getOtp(), OtpType.REGISTRATION);

        // Kích hoạt tài khoản
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendRegisterOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));

        if (user.isEnabled()) {
            throw new IllegalStateException("Tài khoản này đã kích hoạt rồi!");
        }

        otpService.resendOtp(email, OtpType.REGISTRATION);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản nào gắn với email: " + forgotPasswordDTO.getEmail()));

        otpService.generateAndSendOtp(user.getEmail(), OtpType.RESET_PASSWORD);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp!");
        }

        User user = userRepository.findByEmail(resetPasswordDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email này!"));

        // Xác thực OTP đặt lại mật khẩu
        otpService.verifyOtp(resetPasswordDTO.getEmail(), resetPasswordDTO.getOtp(), OtpType.RESET_PASSWORD);

        // Đổi mật khẩu mới đã băm BCrypt
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendResetOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này!"));

        otpService.resendOtp(email, OtpType.RESET_PASSWORD);
    }

    @Override
    public Page<UserDTO> findUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchUsers(keyword.trim(), pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(userMapper::toDTO);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void createUserByAdmin(UserDTO userDTO, String rawPassword) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username '" + userDTO.getUsername() + "' đã tồn tại!");
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDTO.getEmail() + "' đã tồn tại!");
        }

        Role role = resolveRole(userDTO.getRoleName());

        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(rawPassword != null && !rawPassword.isEmpty() ? rawPassword : "123456"))
                .enabled(userDTO.isEnabled())
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserByAdmin(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

        // Kiểm tra trùng email nếu đổi
        if (!user.getEmail().equalsIgnoreCase(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDTO.getEmail() + "' đã được sử dụng bởi tài khoản khác!");
        }

        user.setEmail(userDTO.getEmail());
        user.setEnabled(userDTO.isEnabled());

        if (userDTO.getRoleName() != null && !userDTO.getRoleName().isEmpty()) {
            user.setRole(resolveRole(userDTO.getRoleName()));
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));
        userRepository.delete(user);
    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }

    private Role resolveRole(String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            roleName = "ROLE_USER";
        }
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        final String targetName = roleName;
        return roleRepository.findByName(targetName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(targetName).build()));
    }
}

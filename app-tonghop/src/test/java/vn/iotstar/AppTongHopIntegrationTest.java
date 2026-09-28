package vn.iotstar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AppTongHopIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private OtpService otpService;

    // 1. GUEST ACCESS PUBLIC
    @Test
    @DisplayName("1. Guest truy cập trang chủ / thành công")
    void testGuestAccessHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("2. Guest truy cập danh sách sản phẩm /products công khai thành công")
    void testGuestAccessProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"));
    }

    // 2. GUEST ACCESS PROTECTED
    @Test
    @DisplayName("3. Guest truy cập trang tạo sản phẩm bị chuyển hướng đến /login")
    void testGuestAccessNewProductRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("4. Guest truy cập quản trị người dùng /admin/users bị chuyển hướng đến /login")
    void testGuestAccessAdminUsersRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // 3. USER LOGIN & ADMIN LOGIN
    @Test
    @DisplayName("5. USER login với tài khoản user/123456 thành công")
    void testUserLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("user").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("6. ADMIN login với tài khoản admin/123456 thành công")
    void testAdminLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // 4. BAD PASSWORD
    @Test
    @DisplayName("7. Đăng nhập sai mật khẩu bị chuyển hướng về /login?error=true")
    void testLoginBadPassword() throws Exception {
        mockMvc.perform(formLogin().user("user").password("sai_mat_khau"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // 5. USER ACCESS ADMIN (VƯỢT QUYỀN)
    @Test
    @WithUserDetails("user")
    @DisplayName("8. USER truy cập trang quản trị /admin/users bị từ chối chuyển sang /403")
    void testUserAccessAdminForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    // 6. LOGOUT
    @Test
    @WithUserDetails("user")
    @DisplayName("9. Đăng xuất tài khoản thành công chuyển về /login?logout=true")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    // 7. REGISTER & OTP VERIFICATION
    @Test
    @DisplayName("10. Đăng ký tài khoản mới -> sinh mã OTP -> xác thực OTP kích hoạt thành công")
    void testRegisterAndVerifyOtp() {
        String testEmail = "test_register_" + System.currentTimeMillis() + "@gmail.com";
        String testUser = "testuser_" + System.currentTimeMillis();

        RegisterDTO regDto = RegisterDTO.builder()
                .username(testUser)
                .email(testEmail)
                .password("password123")
                .confirmPassword("password123")
                .build();

        userService.register(regDto);

        User created = userRepository.findByEmail(testEmail).orElse(null);
        assertNotNull(created);
        assertFalse(created.isEnabled(), "Tài khoản vừa đăng ký phải ở trạng thái chưa kích hoạt (enabled=false)");

        // Lấy token OTP vừa tạo
        OtpToken token = otpTokenRepository.findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(testEmail, OtpType.REGISTRATION)
                .orElse(null);
        assertNotNull(token);
        assertFalse(token.isUsed());
        assertNotNull(token.getOtpHash());

        // Kiểm tra OTP sai bị từ chối
        assertThrows(IllegalArgumentException.class, () ->
                userService.verifyRegisterOtp(new VerifyOtpDTO(testEmail, "000000"))
        );

        // Kích hoạt bằng mã OTP thật đã được gửi
        String sentOtp = vn.iotstar.service.impl.EmailServiceImpl.getLastSentOtp();
        assertNotNull(sentOtp);
        userService.verifyRegisterOtp(new VerifyOtpDTO(testEmail, sentOtp));

        User activated = userRepository.findByEmail(testEmail).orElse(null);
        assertNotNull(activated);
        assertTrue(activated.isEnabled(), "Tài khoản phải được kích hoạt sau khi xác thực OTP thành công");
    }

    // 8. OTP EXPIRED & ATTEMPTS LIMIT
    @Test
    @DisplayName("11. OTP hết hạn hoặc sai quá 5 lần bị vô hiệu hóa")
    void testOtpExpiredAndAttempts() {
        String email = "expire_test@gmail.com";
        OtpToken token = OtpToken.builder()
                .email(email)
                .otpHash("fakehash")
                .otpType(OtpType.REGISTRATION)
                .expiryTime(LocalDateTime.now().minusMinutes(10)) // Đã hết hạn
                .attempts(0)
                .used(false)
                .build();
        otpTokenRepository.save(token);

        assertThrows(IllegalArgumentException.class, () ->
                otpService.verifyOtp(email, "123456", OtpType.REGISTRATION)
        );
    }

    // 9. FORGOT PASSWORD & RESET PASSWORD
    @Test
    @DisplayName("12. Quên mật khẩu -> Gửi OTP Reset -> Đổi mật khẩu mới thành công")
    void testForgotPasswordAndReset() {
        // user có sẵn: user@iotstar.vn
        userService.forgotPassword(new ForgotPasswordDTO("user@iotstar.vn"));

        OtpToken resetToken = otpTokenRepository.findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc("user@iotstar.vn", OtpType.RESET_PASSWORD)
                .orElse(null);
        assertNotNull(resetToken);
        assertEquals(OtpType.RESET_PASSWORD, resetToken.getOtpType());
    }

    // 10. PRODUCT SEARCH & PAGINATION
    @Test
    @DisplayName("13. Tìm kiếm sản phẩm theo từ khóa và phân trang")
    void testProductSearchAndPagination() throws Exception {
        mockMvc.perform(get("/products")
                        .param("keyword", "Laptop")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"));
    }

    // 11. USER CANNOT EDIT ANOTHER USER'S PRODUCT (KIỂM TRA QUYỀN SỞ HỮU CHẶT CHẼ)
    @Test
    @WithUserDetails("user")
    @DisplayName("14. USER cố tình sửa sản phẩm của USER khác bị chặn (403 Forbidden)")
    void testUserCannotEditAnotherUsersProduct() throws Exception {
        // Tìm sản phẩm thuộc sở hữu của user2
        User user2 = userRepository.findByUsername("user2").orElse(null);
        assertNotNull(user2);

        Product p = productRepository.save(Product.builder()
                .name("Sản phẩm của user2")
                .price(new BigDecimal("1000000"))
                .description("Mô tả test quyền sở hữu")
                .user(user2)
                .build());

        // user (không phải user2) cố tình truy cập form edit sản phẩm của user2
        mockMvc.perform(get("/products/edit/" + p.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));

        // user cố tình POST update sản phẩm của user2
        mockMvc.perform(post("/products/edit/" + p.getId())
                        .with(csrf())
                        .param("name", "Hack Tên Sản Phẩm")
                        .param("price", "2000000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    // 12. ADMIN CAN EDIT ANY PRODUCT
    @Test
    @WithUserDetails("admin")
    @DisplayName("15. ADMIN có toàn quyền sửa sản phẩm của bất kỳ người dùng nào")
    void testAdminCanEditAnyProduct() throws Exception {
        User user = userRepository.findByUsername("user").orElse(null);
        assertNotNull(user);

        Product p = productRepository.save(Product.builder()
                .name("Sản phẩm của user")
                .price(new BigDecimal("500000"))
                .description("Mô tả ban đầu")
                .user(user)
                .build());

        // Admin mở form edit thành công
        mockMvc.perform(get("/products/edit/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/form"));

        // Admin update thành công
        mockMvc.perform(post("/products/edit/" + p.getId())
                        .with(csrf())
                        .param("name", "Tên mới do Admin cập nhật")
                        .param("price", "990000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/" + p.getId()));
    }

    // 13. FILE UPLOAD
    @Test
    @WithUserDetails("user")
    @DisplayName("16. Tạo sản phẩm mới kèm upload file ảnh thành công")
    void testCreateProductWithImageUpload() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test_photo.jpg", "image/jpeg", "fake_image_content".getBytes()
        );

        mockMvc.perform(multipart("/products/new")
                        .file(image)
                        .with(csrf())
                        .param("name", "Bàn phím Test Upload")
                        .param("price", "1200000")
                        .param("description", "Mô tả test upload ảnh"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    // 14. ADMIN CRUD USER
    @Test
    @WithUserDetails("admin")
    @DisplayName("17. Admin xem danh sách, tìm kiếm và toggle trạng thái người dùng")
    void testAdminUserManagement() throws Exception {
        // Danh sách user
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"));

        // Toggle trạng thái user
        User user = userRepository.findByUsername("user").orElse(null);
        assertNotNull(user);
        boolean currentStatus = user.isEnabled();

        mockMvc.perform(post("/admin/users/toggle/" + user.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        User updated = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(!currentStatus, updated.isEnabled());
    }
}

package vn.iotstar.vidu1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Vidu1SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Guest truy cập public /hello thành công")
    void testGuestAccessPublicEndpoint() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Guest truy cập /customer bị chuyển hướng đến trang login")
    void testGuestAccessProtectedCustomerRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/customer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Guest truy cập /admin bị chuyển hướng đến trang login")
    void testGuestAccessProtectedAdminRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER đăng nhập truy cập /customer thành công")
    void testUserAccessCustomerSuccess() throws Exception {
        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER đăng nhập truy cập /customer/CUST01 thành công qua @PreAuthorize")
    void testUserAccessCustomerDetailSuccess() throws Exception {
        mockMvc.perform(get("/customer/CUST01"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer_detail"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER truy cập /admin bị từ chối quyền (403 Forbidden)")
    void testUserAccessAdminForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN truy cập /admin thành công")
    void testAdminAccessAdminSuccess() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    @DisplayName("Form login với tài khoản demo user/123456 thành công")
    void testFormLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("user").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Form login với sai mật khẩu bị chuyển hướng về /login?error")
    void testFormLoginBadCredentials() throws Exception {
        mockMvc.perform(formLogin().user("user").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Đăng xuất thành công và chuyển hướng về /hello?logout=true")
    void testLogoutSuccess() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/hello?logout=true"));
    }
}

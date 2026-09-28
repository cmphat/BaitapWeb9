package vn.iotstar.vidu3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Vidu3ThymeleafSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Guest truy cập public trang chủ / thành công")
    void testGuestAccessHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("Guest truy cập trang đăng nhập /login thành công")
    void testGuestAccessLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Guest truy cập trang /403 thành công")
    void testGuestAccess403Page() throws Exception {
        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("403"));
    }

    @Test
    @DisplayName("Guest truy cập /user/dashboard bị chuyển hướng đến /login")
    void testGuestAccessUserDashboardRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Guest truy cập /admin/dashboard bị chuyển hướng đến /login")
    void testGuestAccessAdminDashboardRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Đăng nhập bằng user/123456 thành công")
    void testUserLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("user").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithUserDetails("user")
    @DisplayName("USER đăng nhập truy cập /user/dashboard thành công")
    void testUserAccessUserDashboardSuccess() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"));
    }

    @Test
    @WithUserDetails("user")
    @DisplayName("USER đăng nhập truy cập /admin/dashboard bị từ chối chuyển sang /403")
    void testUserAccessAdminDashboardForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/403"));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("ADMIN đăng nhập truy cập /admin/dashboard thành công")
    void testAdminAccessAdminDashboardSuccess() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }

    @Test
    @WithUserDetails("admin")
    @DisplayName("ADMIN đăng nhập cũng có thể truy cập /user/dashboard thành công")
    void testAdminAccessUserDashboardSuccess() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/dashboard"));
    }
}

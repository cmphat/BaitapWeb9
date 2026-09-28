package vn.iotstar.vidu2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class Vidu2SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Guest truy cập public /hello thành công")
    void testGuestAccessPublic() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Guest truy cập /customer/all bị chuyển hướng đến /login")
    void testGuestAccessProtectedCustomerRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/customer/all"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Guest truy cập /admin bị chuyển hướng đến /login")
    void testGuestAccessProtectedAdminRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Đăng nhập bằng tài khoản Database user_db/123456 thành công")
    void testDatabaseUserLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("user_db").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Đăng nhập bằng tài khoản Database admin_db/123456 thành công")
    void testDatabaseAdminLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("admin_db").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu bị chuyển hướng về /login?error=true")
    void testDatabaseLoginBadPassword() throws Exception {
        mockMvc.perform(formLogin().user("user_db").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("Tài khoản disabled_user bị vô hiệu hóa không thể đăng nhập")
    void testDisabledUserCannotLogin() throws Exception {
        mockMvc.perform(formLogin().user("disabled_user").password("123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithUserDetails("user_db")
    @DisplayName("user_db đăng nhập truy cập /customer/all thành công")
    void testUserDbAccessCustomerSuccess() throws Exception {
        mockMvc.perform(get("/customer/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer"));
    }

    @Test
    @WithUserDetails("user_db")
    @DisplayName("user_db truy cập /admin bị từ chối quyền (403 Forbidden)")
    void testUserDbAccessAdminForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin_db")
    @DisplayName("admin_db truy cập /admin thành công")
    void testAdminDbAccessAdminSuccess() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }
}

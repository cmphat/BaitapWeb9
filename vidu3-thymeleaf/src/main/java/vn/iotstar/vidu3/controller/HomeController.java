package vn.iotstar.vidu3.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Đăng xuất tài khoản thành công!");
        }
        return "login";
    }

    @GetMapping("/403")
    public String accessDenied(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "403";
    }
}

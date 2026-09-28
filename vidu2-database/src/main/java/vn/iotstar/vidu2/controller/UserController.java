package vn.iotstar.vidu2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.vidu2.entity.UserInfo;
import vn.iotstar.vidu2.service.UserInfoService;

import java.util.List;

@Controller
public class UserController {

    private final UserInfoService userInfoService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserInfoService userInfoService, PasswordEncoder passwordEncoder) {
        this.userInfoService = userInfoService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        model.addAttribute("users", userInfoService.getAllUsers());
        return "index";
    }

    @GetMapping("/hello")
    public String hello(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Đăng nhập thất bại! Vui lòng kiểm tra lại tài khoản, mật khẩu hoặc tài khoản có thể đã bị vô hiệu hóa (disabled).");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất an toàn khỏi hệ thống.");
        }
        return "login";
    }

    @PostMapping("/user/new")
    public String addNewUserFromForm(@ModelAttribute UserInfo userInfo, Model model) {
        try {
            userInfoService.addUser(userInfo, passwordEncoder);
            return "redirect:/?success=true";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }

    @PostMapping("/api/user/new")
    @ResponseBody
    public ResponseEntity<String> addNewUserApi(@RequestBody UserInfo userInfo) {
        try {
            String result = userInfoService.addUser(userInfo, passwordEncoder);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<List<UserInfo>> getAllUsersApi() {
        return ResponseEntity.ok(userInfoService.getAllUsers());
    }

    @GetMapping("/customer/all")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public String customerAll(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "customer";
    }

    @GetMapping("/customer/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public String customerDetail(@PathVariable("id") String id, Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        model.addAttribute("customerId", id);
        return "customer";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminPage(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "admin";
    }
}

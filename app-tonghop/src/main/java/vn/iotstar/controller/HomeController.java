package vn.iotstar.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
public class HomeController {

    private final ProductService productService;
    private final UserService userService;

    public HomeController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        // Lấy 4 sản phẩm mới nhất hiển thị trang chủ
        model.addAttribute("recentProducts", productService.findProducts("", PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("totalUsers", userService.getTotalUserCount());
        model.addAttribute("totalProducts", productService.getTotalProductCount());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/user/dashboard";
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String userDashboard(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "user/dashboard";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        model.addAttribute("totalUsers", userService.getTotalUserCount());
        model.addAttribute("totalProducts", productService.getTotalProductCount());
        return "admin/dashboard";
    }

    @GetMapping("/403")
    public String accessDenied(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "error/403";
    }
}

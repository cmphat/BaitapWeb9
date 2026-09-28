package vn.iotstar.vidu3.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

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
        return "admin/dashboard";
    }
}

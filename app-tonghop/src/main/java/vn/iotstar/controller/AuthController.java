package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "registered", required = false) String registered,
                            @RequestParam(value = "reset", required = false) String reset,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tài khoản hoặc mật khẩu không chính xác, hoặc tài khoản chưa kích hoạt/đã bị khóa.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất tài khoản thành công!");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
        }
        if (reset != null) {
            model.addAttribute("successMessage", "Đặt lại mật khẩu thành công! Hãy đăng nhập với mật khẩu mới.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(registerDTO);
            redirectAttributes.addFlashAttribute("infoMessage", "Mã xác thực OTP gồm 6 chữ số đã được gửi tới email: " + registerDTO.getEmail());
            return "redirect:/verify-otp?email=" + registerDTO.getEmail();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(value = "email", required = false) String email, Model model) {
        VerifyOtpDTO dto = new VerifyOtpDTO();
        if (email != null) {
            dto.setEmail(email);
        }
        model.addAttribute("verifyOtpDTO", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO verifyOtpDTO,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        try {
            userService.verifyRegisterOtp(verifyOtpDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt tài khoản thành công! Hãy đăng nhập để trải nghiệm.");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-register-otp")
    public String resendRegisterOtp(@RequestParam("email") String email,
                                    RedirectAttributes redirectAttributes) {
        try {
            userService.resendRegisterOtp(email);
            redirectAttributes.addFlashAttribute("infoMessage", "Đã gửi lại mã OTP mới tới email: " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/verify-otp?email=" + email;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO forgotPasswordDTO,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            userService.forgotPassword(forgotPasswordDTO);
            redirectAttributes.addFlashAttribute("infoMessage", "Mã xác thực đổi mật khẩu đã được gửi đến email của bạn.");
            return "redirect:/reset-password?email=" + forgotPasswordDTO.getEmail();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "email", required = false) String email, Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        if (email != null) {
            dto.setEmail(email);
        }
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO resetPasswordDTO,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            userService.resetPassword(resetPasswordDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Hãy đăng nhập lại.");
            return "redirect:/login?reset=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }

    @PostMapping("/resend-reset-otp")
    public String resendResetOtp(@RequestParam("email") String email,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.resendResetOtp(email);
            redirectAttributes.addFlashAttribute("infoMessage", "Đã gửi lại mã OTP đặt lại mật khẩu mới tới: " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reset-password?email=" + email;
    }
}

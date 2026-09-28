package vn.iotstar.vidu1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.iotstar.vidu1.model.Customer;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CustomerController {

    private final List<Customer> customers = new ArrayList<>();

    public CustomerController() {
        customers.add(new Customer("CUST01", "Nguyễn Văn An", "an.nguyen@example.com", "0901234567"));
        customers.add(new Customer("CUST02", "Trần Thị Bình", "binh.tran@example.com", "0912345678"));
        customers.add(new Customer("CUST03", "Lê Hoàng Cường", "cuong.le@example.com", "0923456789"));
    }

    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "index";
    }

    @GetMapping("/hello")
    public String hello(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "index";
    }

    @GetMapping("/customer")
    public String getCustomerList(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        model.addAttribute("customers", customers);
        return "customer";
    }

    @GetMapping("/api/customer")
    @ResponseBody
    public ResponseEntity<List<Customer>> getCustomerListApi() {
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customer/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public String getCustomerById(@PathVariable("id") String id, Authentication authentication, Model model) {
        Customer customer = customers.stream()
                .filter(c -> c.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
        model.addAttribute("auth", authentication);
        model.addAttribute("customer", customer);
        model.addAttribute("customerId", id);
        return "customer_detail";
    }

    @GetMapping("/api/customer/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Customer> getCustomerByIdApi(@PathVariable("id") String id) {
        return customers.stream()
                .filter(c -> c.getId().equalsIgnoreCase(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminPage(Authentication authentication, Model model) {
        model.addAttribute("auth", authentication);
        return "admin";
    }

    @GetMapping("/api/admin")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> adminApi() {
        return ResponseEntity.ok("Xin chào ADMIN! Bạn đang truy cập API quản trị với toàn quyền.");
    }
}

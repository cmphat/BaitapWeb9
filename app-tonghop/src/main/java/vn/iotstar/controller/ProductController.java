package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private boolean checkIsAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "6") int size,
                               Authentication authentication,
                               Model model) {
        Page<ProductDTO> productPage = productService.findProducts(keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("products", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("auth", authentication);
        model.addAttribute("isAdmin", checkIsAdmin(authentication));
        return "products/list";
    }

    @GetMapping("/{id:[0-9]+}")
    public String productDetail(@PathVariable("id") Long id,
                                Authentication authentication,
                                Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("auth", authentication);
        boolean isOwner = authentication != null && authentication.getName().equals(product.getUserUsername());
        boolean isAdmin = checkIsAdmin(authentication);
        model.addAttribute("canEdit", isOwner || isAdmin);
        return "products/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String newProductForm(Model model) {
        if (!model.containsAttribute("productDTO")) {
            model.addAttribute("productDTO", new ProductDTO());
        }
        model.addAttribute("isEdit", false);
        return "products/form";
    }

    @PostMapping("/new")
    @PreAuthorize("isAuthenticated()")
    public String createProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                BindingResult bindingResult,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "products/form";
        }

        try {
            // Không tin cậy userId gửi từ browser, lấy trực tiếp từ Authentication principal
            String currentUsername = authentication.getName();
            productService.createProduct(productDTO, imageFile, currentUsername);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", e.getMessage());
            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProductForm(@PathVariable("id") Long id,
                                  Authentication authentication,
                                  Model model) {
        ProductDTO product = productService.getProductById(id);

        boolean isAdmin = checkIsAdmin(authentication);
        boolean isOwner = authentication.getName().equals(product.getUserUsername());

        // Kiểm tra quyền sở hữu nghiêm ngặt trước khi hiển thị form
        if (!isAdmin && !isOwner) {
            return "redirect:/403";
        }

        model.addAttribute("productDTO", product);
        model.addAttribute("isEdit", true);
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String updateProduct(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                BindingResult bindingResult,
                                @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "products/form";
        }

        try {
            String currentUsername = authentication.getName();
            boolean isAdmin = checkIsAdmin(authentication);
            productService.updateProduct(id, productDTO, imageFile, currentUsername, isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/products/" + id;
        } catch (SecurityException se) {
            return "redirect:/403";
        } catch (Exception e) {
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", e.getMessage());
            return "products/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProduct(@PathVariable("id") Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String currentUsername = authentication.getName();
            boolean isAdmin = checkIsAdmin(authentication);
            productService.deleteProduct(id, currentUsername, isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (SecurityException se) {
            return "redirect:/403";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }
}

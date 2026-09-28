package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;

public interface ProductService {

    Page<ProductDTO> findProducts(String keyword, Pageable pageable);

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile, String currentUsername);

    ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile imageFile, String currentUsername, boolean isAdmin);

    void deleteProduct(Long id, String currentUsername, boolean isAdmin);

    long getTotalProductCount();
}

package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.ProductService;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    public ProductServiceImpl(ProductRepository productRepository,
                              UserRepository userRepository,
                              ProductMapper productMapper,
                              CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productMapper = productMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public Page<ProductDTO> findProducts(String keyword, Pageable pageable) {
        Page<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productRepository.searchProducts(keyword.trim(), pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        return products.map(productMapper::toDTO);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile, String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng hiện tại: " + currentUsername));

        String imageUrl = null;
        String cloudinaryPublicId = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            Map<String, String> uploadResult = cloudinaryService.uploadImage(imageFile);
            imageUrl = uploadResult.get("url");
            cloudinaryPublicId = uploadResult.get("public_id");
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .description(productDTO.getDescription())
                .imageUrl(imageUrl)
                .cloudinaryPublicId(cloudinaryPublicId)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        Product saved = productRepository.save(product);
        return productMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile imageFile, String currentUsername, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + id));

        // Kiểm tra quyền sở hữu nghiêm ngặt tại backend
        if (!isAdmin && !product.getUser().getUsername().equals(currentUsername)) {
            throw new SecurityException("Từ chối truy cập: Bạn chỉ được phép sửa sản phẩm do chính mình tạo ra!");
        }

        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());

        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (product.getCloudinaryPublicId() != null) {
                cloudinaryService.deleteImage(product.getCloudinaryPublicId());
            }

            Map<String, String> uploadResult = cloudinaryService.uploadImage(imageFile);
            product.setImageUrl(uploadResult.get("url"));
            product.setCloudinaryPublicId(uploadResult.get("public_id"));
        }

        Product updated = productRepository.save(product);
        return productMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String currentUsername, boolean isAdmin) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + id));

        // Kiểm tra quyền sở hữu nghiêm ngặt tại backend
        if (!isAdmin && !product.getUser().getUsername().equals(currentUsername)) {
            throw new SecurityException("Từ chối truy cập: Bạn chỉ được phép xóa sản phẩm do chính mình tạo ra!");
        }

        if (product.getCloudinaryPublicId() != null) {
            cloudinaryService.deleteImage(product.getCloudinaryPublicId());
        }

        productRepository.delete(product);
    }

    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }
}

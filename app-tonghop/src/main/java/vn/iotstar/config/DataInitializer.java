package vn.iotstar.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Khởi tạo Roles
        Role roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_USER").build())
        );

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_ADMIN").build())
        );

        // 2. Khởi tạo Admin mặc định (admin / 123456)
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User newAdmin = User.builder()
                    .username("admin")
                    .email("admin@iotstar.vn")
                    .password(passwordEncoder.encode("123456"))
                    .enabled(true)
                    .role(roleAdmin)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newAdmin);
        });

        // 3. Khởi tạo User mặc định (user / 123456)
        User user = userRepository.findByUsername("user").orElseGet(() -> {
            User newUser = User.builder()
                    .username("user")
                    .email("user@iotstar.vn")
                    .password(passwordEncoder.encode("123456"))
                    .enabled(true)
                    .role(roleUser)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        // 4. Khởi tạo User 2 mặc định (user2 / 123456)
        User user2 = userRepository.findByUsername("user2").orElseGet(() -> {
            User newUser2 = User.builder()
                    .username("user2")
                    .email("user2@iotstar.vn")
                    .password(passwordEncoder.encode("123456"))
                    .enabled(true)
                    .role(roleUser)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser2);
        });

        // 5. Khởi tạo sản phẩm mẫu nếu chưa có
        if (productRepository.count() == 0) {
            Product p1 = Product.builder()
                    .name("Laptop Dell XPS 15 (2024)")
                    .price(new BigDecimal("35000000"))
                    .description("Laptop cao cấp màn hình OLED 3.5K, Intel Core i9, 32GB RAM chuyên cho đồ họa và lập trình.")
                    .imageUrl("https://images.unsplash.com/photo-1593642632823-8f785ba67e45?w=500")
                    .createdAt(LocalDateTime.now())
                    .user(admin)
                    .build();

            Product p2 = Product.builder()
                    .name("Bàn phím cơ không dây Keychron K2 Pro")
                    .price(new BigDecimal("1850000"))
                    .description("Bàn phím cơ layout 75%, kết nối Bluetooth/Type-C, switch Gateron Pro gõ êm ái.")
                    .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500")
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();

            Product p3 = Product.builder()
                    .name("Chuột công thái học Logitech MX Master 3S")
                    .price(new BigDecimal("2250000"))
                    .description("Chuột không dây yên tĩnh, cảm biến Darkfield 8000 DPI, cuộn vô cực MagSpeed cao cấp.")
                    .imageUrl("https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=500")
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();

            Product p4 = Product.builder()
                    .name("Tai nghe chống ồn Sony WH-1000XM5")
                    .price(new BigDecimal("6990000"))
                    .description("Tai nghe over-ear chống ồn chủ động hàng đầu thế giới, thời lượng pin 30 giờ.")
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500")
                    .createdAt(LocalDateTime.now())
                    .user(user2)
                    .build();

            productRepository.save(p1);
            productRepository.save(p2);
            productRepository.save(p3);
            productRepository.save(p4);
        }
    }
}

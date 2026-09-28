package vn.iotstar.vidu2.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.iotstar.vidu2.entity.UserInfo;
import vn.iotstar.vidu2.repository.UserInfoRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserInfoRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserInfoRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Idempotent seeding for demo users in SQL Server
        if (!repository.existsByName("user_db")) {
            UserInfo user = new UserInfo();
            user.setName("user_db");
            user.setEmail("user_db@example.com");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setRoles("ROLE_USER");
            user.setEnabled(true);
            repository.save(user);
        }

        if (!repository.existsByName("admin_db")) {
            UserInfo admin = new UserInfo();
            admin.setName("admin_db");
            admin.setEmail("admin_db@example.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRoles("ROLE_ADMIN");
            admin.setEnabled(true);
            repository.save(admin);
        }

        if (!repository.existsByName("disabled_user")) {
            UserInfo disabled = new UserInfo();
            disabled.setName("disabled_user");
            disabled.setEmail("disabled@example.com");
            disabled.setPassword(passwordEncoder.encode("123456"));
            disabled.setRoles("ROLE_USER");
            disabled.setEnabled(false); // Disabled account to test login refusal
            repository.save(disabled);
        }
    }
}

package vn.iotstar.vidu2.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iotstar.vidu2.entity.UserInfo;
import vn.iotstar.vidu2.repository.UserInfoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository repository;

    public UserInfoService(UserInfoRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userInfo = repository.findByName(username);
        if (userInfo.isEmpty()) {
            userInfo = repository.findByEmail(username);
        }

        return userInfo.map(UserInfoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username hoặc email: " + username));
    }

    public String addUser(UserInfo userInfo, PasswordEncoder passwordEncoder) {
        if (repository.existsByName(userInfo.getName())) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại: " + userInfo.getName());
        }
        if (repository.existsByEmail(userInfo.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + userInfo.getEmail());
        }
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "Thêm người dùng thành công!";
    }

    public List<UserInfo> getAllUsers() {
        return repository.findAll();
    }
}

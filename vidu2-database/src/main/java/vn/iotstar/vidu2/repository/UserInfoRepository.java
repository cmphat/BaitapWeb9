package vn.iotstar.vidu2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.vidu2.entity.UserInfo;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByName(String name);
    Optional<UserInfo> findByEmail(String email);
    Optional<UserInfo> findByNameOrEmail(String name, String email);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}

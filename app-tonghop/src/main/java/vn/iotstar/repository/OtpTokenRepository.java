package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(String email, OtpType otpType);

    List<OtpToken> findByEmailAndOtpType(String email, OtpType otpType);

    void deleteByEmail(String email);
}

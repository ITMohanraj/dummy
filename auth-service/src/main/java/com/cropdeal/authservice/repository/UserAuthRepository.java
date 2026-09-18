package com.cropdeal.authservice.repository;

import com.cropdeal.authservice.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByEmail(String email);
    Optional<UserAuth> findByFacebookId(String facebookId);
    boolean existsByEmail(String email);
}

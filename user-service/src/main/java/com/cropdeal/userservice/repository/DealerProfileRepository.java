package com.cropdeal.userservice.repository;

import com.cropdeal.userservice.entity.DealerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerProfileRepository extends JpaRepository<DealerProfile, Long> {
    Optional<DealerProfile> findByUserId(Long userId);
}

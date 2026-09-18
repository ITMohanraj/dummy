package com.cropdeal.userservice.repository;

import com.cropdeal.userservice.entity.DeliveryPartnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryPartnerProfileRepository extends JpaRepository<DeliveryPartnerProfile, Long> {
    Optional<DeliveryPartnerProfile> findByUserId(Long userId);
}

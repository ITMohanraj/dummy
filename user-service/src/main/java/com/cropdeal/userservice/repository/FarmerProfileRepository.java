package com.cropdeal.userservice.repository;

import com.cropdeal.userservice.entity.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Long> {
    Optional<FarmerProfile> findByUserId(Long userId);
}

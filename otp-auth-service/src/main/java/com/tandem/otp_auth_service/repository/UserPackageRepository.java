package com.tandem.otp_auth_service.repository;

import com.tandem.otp_auth_service.entity.UserPackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPackageRepository extends JpaRepository<UserPackages, String> {


    Optional<UserPackages> findByUserId(String userId);

}

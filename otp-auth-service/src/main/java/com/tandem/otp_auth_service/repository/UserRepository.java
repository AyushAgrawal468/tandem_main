package com.tandem.otp_auth_service.repository;

import com.tandem.otp_auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPhoneNo(String normalizedPhone);

    Optional<User> findByUserId(String userId);
}

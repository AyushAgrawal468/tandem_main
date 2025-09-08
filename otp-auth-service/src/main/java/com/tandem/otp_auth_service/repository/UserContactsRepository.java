package com.tandem.otp_auth_service.repository;

import com.tandem.otp_auth_service.entity.UserContacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserContactsRepository extends JpaRepository<UserContacts, String> {
    List<UserContacts> findByUserId(String userId);
    // You can add custom query methods here if needed
}


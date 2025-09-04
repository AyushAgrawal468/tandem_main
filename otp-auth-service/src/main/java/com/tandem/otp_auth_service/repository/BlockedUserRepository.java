package com.tandem.otp_auth_service.repository;

import com.tandem.otp_auth_service.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser,String> {

    Optional<BlockedUser > findByPhoneNo(String phoneNo);
}

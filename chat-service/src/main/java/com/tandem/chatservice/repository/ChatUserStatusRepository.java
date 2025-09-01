package com.tandem.chatservice.repository;

import com.tandem.chatservice.entity.ChatUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatUserStatusRepository extends JpaRepository<ChatUserStatus, String> {

    List<ChatUserStatus> findByOnlineTrue();

    @Modifying
    @Query("UPDATE ChatUserStatus u SET u.online = :status, u.lastSeen = :lastSeen WHERE u.phoneNumber = :phoneNumber")
    void updateUserStatus(String phoneNumber, boolean status, LocalDateTime lastSeen);

    @Query("SELECT u FROM ChatUserStatus u WHERE u.phoneNumber IN :phoneNumbers")
    List<ChatUserStatus> findByPhoneNumbers(List<String> phoneNumbers);
}

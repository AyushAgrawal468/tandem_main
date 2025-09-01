package com.tandem.chatservice.repository;

import com.tandem.chatservice.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("SELECT cr FROM ChatRoom cr WHERE :phoneNumber MEMBER OF cr.participants")
    List<ChatRoom> findRoomsByParticipant(String phoneNumber);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = 'DIRECT' AND :phone1 MEMBER OF cr.participants AND :phone2 MEMBER OF cr.participants")
    Optional<ChatRoom> findDirectChatRoom(String phone1, String phone2);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = 'GROUP' AND :phoneNumber MEMBER OF cr.participants")
    List<ChatRoom> findGroupChatsByParticipant(String phoneNumber);
}

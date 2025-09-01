package com.tandem.chatservice.repository;

import com.tandem.chatservice.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId ORDER BY m.sentAt DESC")
    Page<Message> findByRoomIdOrderBySentAtDesc(String roomId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.sentAt > :since ORDER BY m.sentAt ASC")
    List<Message> findRecentMessages(String roomId, LocalDateTime since);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.roomId = :roomId")
    Long countMessagesByRoomId(String roomId);
}

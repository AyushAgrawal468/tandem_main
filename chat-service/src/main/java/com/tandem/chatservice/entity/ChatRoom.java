package com.tandem.chatservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "room_name")
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private RoomType roomType;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @ElementCollection
    @CollectionTable(name = "chat_room_participants",
                    joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "phone_number")
    private Set<String> participants = new HashSet<>();

    public enum RoomType {
        DIRECT, GROUP
    }

    public ChatRoom() {
    }

    public ChatRoom(String id, String roomName, RoomType roomType, String createdBy,
                   LocalDateTime createdAt, LocalDateTime lastActivity, Set<String> participants) {
        this.id = id;
        this.roomName = roomName;
        this.roomType = roomType;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.lastActivity = lastActivity;
        this.participants = participants;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }

    public void addParticipant(String phoneNumber) {
        participants.add(phoneNumber);
    }

    public void removeParticipant(String phoneNumber) {
        participants.remove(phoneNumber);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Set<String> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<String> participants) {
        this.participants = participants;
    }
}

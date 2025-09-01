package com.tandem.chatservice.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class ChatRoomDto {
    private String id;
    private String roomName;
    private String roomType;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private Set<String> participants;
    private ChatMessageDto lastMessage;
    private Long unreadCount;

    public ChatRoomDto() {
    }

    public ChatRoomDto(String id, String roomName, String roomType, String createdBy,
                      LocalDateTime createdAt, LocalDateTime lastActivity, Set<String> participants,
                      ChatMessageDto lastMessage, Long unreadCount) {
        this.id = id;
        this.roomName = roomName;
        this.roomType = roomType;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.lastActivity = lastActivity;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

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

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
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

    public ChatMessageDto getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatMessageDto lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }
}

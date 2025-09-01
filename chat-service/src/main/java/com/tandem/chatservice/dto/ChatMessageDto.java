package com.tandem.chatservice.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {
    private String id;
    private String roomId;
    private String senderPhone;
    private String senderName;
    private String content;
    private String messageType;
    private LocalDateTime sentAt;
    private boolean edited;

    public ChatMessageDto() {
    }

    public ChatMessageDto(String id, String roomId, String senderPhone, String senderName,
                         String content, String messageType, LocalDateTime sentAt, boolean edited) {
        this.id = id;
        this.roomId = roomId;
        this.senderPhone = senderPhone;
        this.senderName = senderName;
        this.content = content;
        this.messageType = messageType;
        this.sentAt = sentAt;
        this.edited = edited;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}

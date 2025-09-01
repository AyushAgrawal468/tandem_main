package com.tandem.chatservice.service;

import com.tandem.chatservice.dto.ChatMessageDto;
import com.tandem.chatservice.dto.ChatRoomDto;
import com.tandem.chatservice.entity.ChatRoom;
import com.tandem.chatservice.entity.ChatUserStatus;
import com.tandem.chatservice.entity.Message;
import com.tandem.chatservice.repository.ChatRoomRepository;
import com.tandem.chatservice.repository.MessageRepository;
import com.tandem.chatservice.repository.ChatUserStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatUserStatusRepository chatUserStatusRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public ChatService(ChatRoomRepository chatRoomRepository,
                      MessageRepository messageRepository,
                      ChatUserStatusRepository chatUserStatusRepository,
                      SimpMessagingTemplate messagingTemplate,
                      UserService userService) {
        this.chatRoomRepository = chatRoomRepository;
        this.messageRepository = messageRepository;
        this.chatUserStatusRepository = chatUserStatusRepository;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @Transactional
    public ChatRoomDto createChatRoom(String creatorPhone, String roomName, Set<String> participants) {
        // Add creator to participants if not already included
        if (!participants.contains(creatorPhone)) {
            participants.add(creatorPhone);
        }

        // Determine chat type based on participant count
        if (participants.size() == 2) {
            // This is a 1:1 chat - check if it already exists
            String[] phones = participants.toArray(new String[0]);
            Optional<ChatRoom> existing = chatRoomRepository.findDirectChatRoom(phones[0], phones[1]);
            if (existing.isPresent()) {
                return convertToDto(existing.get());
            }
        }

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomType(participants.size() == 2 ? ChatRoom.RoomType.DIRECT : ChatRoom.RoomType.GROUP);
        chatRoom.setCreatedBy(creatorPhone);

        // Set room name
        if (roomName != null && !roomName.trim().isEmpty()) {
            chatRoom.setRoomName(roomName);
        } else if (participants.size() == 2) {
            // For 1:1 chats, we can leave roomName null or generate one
            chatRoom.setRoomName(null); // Will be handled by client to show other user's name
        }

        // Add all participants
        participants.forEach(chatRoom::addParticipant);

        chatRoom = chatRoomRepository.save(chatRoom);

        String chatType = participants.size() == 2 ? "direct" : "group";
        log.info("Created {} chat room {} with {} participants", chatType, chatRoom.getId(), participants.size());

        return convertToDto(chatRoom);
    }

    @Transactional
    public ChatMessageDto sendMessage(String roomId, String senderPhone, String content) {
        // Verify room exists and user is participant
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!room.getParticipants().contains(senderPhone)) {
            throw new RuntimeException("User not authorized to send message to this room");
        }

        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderPhone(senderPhone);
        message.setContent(content);
        message.setMessageType(Message.MessageType.TEXT);

        message = messageRepository.save(message);

        // Update room last activity
        room.setLastActivity(LocalDateTime.now());
        chatRoomRepository.save(room);

        ChatMessageDto messageDto = convertToDto(message);

        // Send to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/room/" + roomId, messageDto);

        log.info("Message sent to room {} by {}", roomId, senderPhone);
        return messageDto;
    }

    public List<ChatRoomDto> getUserChatRooms(String phoneNumber) {
        List<ChatRoom> rooms = chatRoomRepository.findRoomsByParticipant(phoneNumber);
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<ChatMessageDto> getRoomMessages(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByRoomIdOrderBySentAtDesc(roomId, pageable);
        return messages.map(this::convertToDto);
    }

    @Transactional
    public void userConnected(String phoneNumber, String username) {
        ChatUserStatus userStatus = chatUserStatusRepository.findById(phoneNumber)
                .orElse(new ChatUserStatus());

        userStatus.setPhoneNumber(phoneNumber);
        userStatus.setOnline(true);
        userStatus.setLastSeen(LocalDateTime.now());

        chatUserStatusRepository.save(userStatus);
        log.info("User {} connected", phoneNumber);
    }

    @Transactional
    public void userDisconnected(String phoneNumber) {
        chatUserStatusRepository.updateUserStatus(phoneNumber, false, LocalDateTime.now());
        log.info("User {} disconnected", phoneNumber);
    }

    private ChatRoomDto convertToDto(ChatRoom room) {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setId(room.getId());
        dto.setRoomName(room.getRoomName());
        dto.setRoomType(room.getRoomType().name());
        dto.setCreatedBy(room.getCreatedBy());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setLastActivity(room.getLastActivity());
        dto.setParticipants(room.getParticipants());

        // TODO: Add last message and unread count logic
        dto.setUnreadCount(0L);

        return dto;
    }

    private ChatMessageDto convertToDto(Message message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setRoomId(message.getRoomId());
        dto.setSenderPhone(message.getSenderPhone());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType().name());
        dto.setSentAt(message.getSentAt());
        dto.setEdited(message.isEdited());

        // Get sender name from OTP-auth-service
        try {
            // This will call the OTP-auth-service to get user details
            // You can replace this with proper service-to-service communication
            String userName = userService.getUserName(message.getSenderPhone());
            dto.setSenderName(userName);
        } catch (Exception e) {
            log.warn("Could not fetch user name for phone: {}", message.getSenderPhone());
            dto.setSenderName("Unknown User");
        }

        return dto;
    }
}

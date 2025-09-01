package com.tandem.chatservice.controller;

import com.tandem.chatservice.dto.ChatMessageDto;
import com.tandem.chatservice.dto.ChatRoomDto;
import com.tandem.chatservice.service.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDto> createChatRoom(@RequestBody Map<String, Object> request) {
        String creatorPhone = (String) request.get("creatorPhone");
        String roomName = (String) request.get("roomName"); // Optional for 1:1 chats
        @SuppressWarnings("unchecked")
        Set<String> participants = (Set<String>) request.get("participants");

        ChatRoomDto room = chatService.createChatRoom(creatorPhone, roomName, participants);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getUserChatRooms(@RequestParam String phoneNumber) {
        List<ChatRoomDto> rooms = chatService.getUserChatRooms(phoneNumber);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageDto>> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ChatMessageDto> messages = chatService.getRoomMessages(roomId, page, size);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(@RequestBody Map<String, String> request) {
        String roomId = request.get("roomId");
        String senderPhone = request.get("senderPhone");
        String content = request.get("content");

        ChatMessageDto message = chatService.sendMessage(roomId, senderPhone, content);
        return ResponseEntity.ok(message);
    }
}

package com.tandem.chatservice.controller;

import com.tandem.chatservice.dto.ChatMessageDto;
import com.tandem.chatservice.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketChatController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketChatController.class);

    private final ChatService chatService;

    public WebSocketChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable String roomId,
                          @Payload Map<String, String> message,
                          SimpMessageHeaderAccessor headerAccessor) {
        try {
            String senderPhone = message.get("senderPhone");
            String content = message.get("content");

            if (senderPhone == null || content == null) {
                log.error("Invalid message format: missing senderPhone or content");
                return;
            }

            ChatMessageDto messageDto = chatService.sendMessage(roomId, senderPhone, content);
            log.info("WebSocket message sent: {}", messageDto.getId());

        } catch (Exception e) {
            log.error("Error sending message via WebSocket: {}", e.getMessage());
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload Map<String, String> user,
                       SimpMessageHeaderAccessor headerAccessor) {
        try {
            String phoneNumber = user.get("phoneNumber");
            String username = user.get("username");

            if (phoneNumber != null && username != null) {
                // Add phone number to WebSocket session
                headerAccessor.getSessionAttributes().put("phoneNumber", phoneNumber);
                chatService.userConnected(phoneNumber, username);
                log.info("User {} joined WebSocket session", phoneNumber);
            }
        } catch (Exception e) {
            log.error("Error adding user to WebSocket: {}", e.getMessage());
        }
    }
}

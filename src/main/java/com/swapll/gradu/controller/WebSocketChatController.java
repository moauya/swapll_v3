package com.swapll.gradu.controller;

import com.swapll.gradu.dto.ChatMessageDTO;
import com.swapll.gradu.dto.ChatSummaryDTO;
import com.swapll.gradu.dto.MessageDTO;
import com.swapll.gradu.model.Chat;
import com.swapll.gradu.model.User;
import com.swapll.gradu.repository.UserRepository;
import com.swapll.gradu.security.JwtUtil;
import com.swapll.gradu.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WebSocketChatController {

    @Autowired private ChatService chatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    // REST: Create or fetch chat summary with a specific user
    @GetMapping("/with/{receiverId}")
    public ChatSummaryDTO getOrCreateChatWithUser(@PathVariable int receiverId,
                                                  @RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        int senderId = Integer.parseInt(jwtUtil.extractUserId(token));
        return chatService.getChatSummary(
                chatService.getOrCreateChat(senderId, receiverId), senderId
        );
    }

    // WebSocket: Send message
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        int userId = Integer.parseInt(jwtUtil.extractUserId(dto.getToken()));
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Invalid sender"));

        Chat chat = chatService.getOrCreateChat(sender.getId(), dto.getReceiverId());
        MessageDTO savedMessage = chatService.saveMessageWithSender(sender, dto);

        messagingTemplate.convertAndSend("/topic/chat." + chat.getId(), savedMessage);
    }

    // WebSocket: Fetch chat messages for a chat
    @MessageMapping("/chat.getMessages.{chatId}")
    @SendTo("/topic/chat.{chatId}")
    public List<MessageDTO> getMessages(@DestinationVariable Integer chatId) {
        return chatService.getChatMessages(chatId);
    }

    // WebSocket: Get inbox (list of all user chats)
    @MessageMapping("/chat.getInbox")
    @SendTo("/topic/inbox")
    public List<ChatSummaryDTO> getInbox(@Payload String token) {
        int userId = Integer.parseInt(jwtUtil.extractUserId(token));
        return chatService.getUserChats(userId);
    }
}

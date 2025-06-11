package com.swapll.gradu.controller;

import com.swapll.gradu.model.Chat;
import com.swapll.gradu.dto.ChatMessageDTO;
import com.swapll.gradu.dto.MessageDTO;
import com.swapll.gradu.service.ChatService;
import com.swapll.gradu.dto.ChatSummaryDTO;
import com.swapll.gradu.repository.UserRepository;
import com.swapll.gradu.model.User;
import com.swapll.gradu.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WebSocketChatController {

    @Autowired private ChatService chatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/with/{receiverId}")
    public ChatSummaryDTO getOrCreateChatWithUser(@PathVariable int receiverId) {
        return chatService.getOrCreateChatWithUser(receiverId);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        // Authenticate sender from JWT token
        String token = dto.getToken();
        Integer userId = Integer.parseInt(jwtUtil.extractUserId(token));
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Invalid sender"));

        Chat chat = chatService.getOrCreateChat(sender.getId(), dto.getReceiverId());
        MessageDTO savedMessage = chatService.saveMessageWithSender(sender, dto);

        messagingTemplate.convertAndSend("/topic/chat." + chat.getId(), savedMessage);
    }

    @MessageMapping("/chat.getMessages.{chatId}")
    @SendTo("/topic/chat.{chatId}")
    public List<MessageDTO> getMessages(@DestinationVariable Integer chatId) {
        return chatService.getChatMessages(chatId);
    }

    @MessageMapping("/chat.getInbox")
    @SendTo("/topic/inbox")
    public List<ChatSummaryDTO> getInbox(@Payload String token) {
        Integer userId = Integer.parseInt(jwtUtil.extractUserId(token));
        return chatService.getUserChats(userId);
    }
}

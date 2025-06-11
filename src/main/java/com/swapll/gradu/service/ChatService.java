package com.swapll.gradu.service;

import com.swapll.gradu.model.Chat;
import com.swapll.gradu.model.Message;
import com.swapll.gradu.model.User;
import com.swapll.gradu.dto.ChatMessageDTO;
import com.swapll.gradu.dto.ChatSummaryDTO;
import com.swapll.gradu.dto.MessageDTO;
import com.swapll.gradu.repository.ChatRepository;
import com.swapll.gradu.repository.MessageRepository;
import com.swapll.gradu.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired private ChatRepository chatRepo;
    @Autowired private MessageRepository messageRepo;
    @Autowired private UserRepository userRepo;

    public Chat getOrCreateChat(Integer senderId, Integer receiverId) {
        return chatRepo.findBySenderIdAndReceiverId(senderId, receiverId)
                .or(() -> chatRepo.findByReceiverIdAndSenderId(senderId, receiverId))
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setSender(userRepo.findById(senderId).orElseThrow());
                    chat.setReceiver(userRepo.findById(receiverId).orElseThrow());
                    return chatRepo.save(chat);
                });
    }

    public MessageDTO saveMessageWithSender(User sender, ChatMessageDTO dto) {
        Chat chat = getOrCreateChat(sender.getId(), dto.getReceiverId());

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setTimestamp(LocalDateTime.now());

        messageRepo.save(message);

        MessageDTO response = new MessageDTO();
        response.setSenderId(sender.getId());
        response.setContent(message.getContent());
        response.setTimestamp(message.getTimestamp());
        return response;
    }

    public List<MessageDTO> getChatMessages(Integer chatId) {
        return messageRepo.findByChatIdOrderByTimestamp(chatId).stream().map(m -> {
            MessageDTO dto = new MessageDTO();
            dto.setSenderId(m.getSender().getId());
            dto.setContent(m.getContent());
            dto.setTimestamp(m.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChatSummaryDTO> getUserChats(Integer userId) {
        return chatRepo.findBySenderIdOrReceiverId(userId, userId).stream().map(chat -> {
            User other = chat.getSender().getId().equals(userId) ? chat.getReceiver() : chat.getSender();
            Message last = messageRepo.findTopByChatIdOrderByTimestampDesc(chat.getId());

            ChatSummaryDTO dto = new ChatSummaryDTO();
            dto.setChatId(chat.getId());
            dto.setOtherUserId(other.getId());
            dto.setOtherUsername(other.getUserName());
            dto.setOtherPicture(other.getProfilePic());
            dto.setLastMessage(last != null ? last.getContent() : null);
            dto.setLastMessageTime(last != null ? last.getTimestamp().toString() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    public ChatSummaryDTO getOrCreateChatWithUser(int receiverId) {
        throw new UnsupportedOperationException("This method must be implemented based on your auth context.");
    }

    public ChatSummaryDTO getChatSummary(Chat chat, int currentUserId) {
        User other = chat.getSender().getId().equals(currentUserId)
                ? chat.getReceiver()
                : chat.getSender();

        Message last = messageRepo.findTopByChatIdOrderByTimestampDesc(chat.getId());

        ChatSummaryDTO dto = new ChatSummaryDTO();
        dto.setChatId(chat.getId());
        dto.setOtherUserId(other.getId());
        dto.setOtherUsername(other.getUserName());
        dto.setOtherPicture(other.getProfilePic());
        dto.setLastMessage(last != null ? last.getContent() : null);
        dto.setLastMessageTime(last != null ? last.getTimestamp().toString() : null);

        return dto;
    }
}
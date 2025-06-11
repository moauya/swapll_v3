package com.swapll.gradu.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {
    private Integer receiverId;
    private String content;
    private String token;

}

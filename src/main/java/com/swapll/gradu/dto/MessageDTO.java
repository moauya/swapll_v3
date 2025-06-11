package com.swapll.gradu.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MessageDTO {
    private Integer senderId;
    private String content;
    private LocalDateTime timestamp;

}

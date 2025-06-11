package com.swapll.gradu.dto;

import com.swapll.gradu.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private int id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private int offerId;

    private String firstName;
    private String lastName;
    private String userName;
    private byte[] profilePicture;


    private int userId;
}

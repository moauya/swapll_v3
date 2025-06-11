package com.swapll.gradu.dto;

import com.swapll.gradu.Enum.OfferType;
import com.swapll.gradu.Enum.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class    OfferDTO {

    private int id;
    private String title;
    private String description;
    private int price;
    private int deliveryTime;
    private PaymentMethod paymentMethod;
    private String image;
    private OfferType type;

    private int OwnerId;
    private LocalDateTime createdAt;
    private Double averageRating;
    private int numberOfReviews;
    //user info
    private String username;
    private String profilePic;
    //category info
    private int categoryId;

}

package com.swapll.gradu.dto.mappers;

import com.swapll.gradu.model.Offer;
import com.swapll.gradu.model.Category;
import com.swapll.gradu.dto.OfferDTO;
import com.swapll.gradu.model.Review;

import java.util.Date;
import java.util.List;

public class OfferMapper {

    public static OfferDTO toDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setTitle(offer.getTitle());
        dto.setDescription(offer.getDescription());
        dto.setPrice(offer.getPrice());
        dto.setDeliveryTime(offer.getDeliveryTime());
        dto.setPaymentMethod(offer.getPaymentMethod());
        dto.setType(offer.getType());
        dto.setCategoryId(offer.getCategory().getId());
        dto.setOwnerId(offer.getOwner().getId());
        dto.setCreatedAt(offer.getCreatedAt());

        dto.setImage(offer.getImage());

        if (offer.getOwner() != null) {
            dto.setUsername(offer.getOwner().getUserName());
        }
        if(!offer.getReviews().isEmpty()){
            dto.setNumberOfReviews(offer.getReviews().size());
        }
        if(offer.getOwner().getProfilePic()!=null){
            dto.setProfilePic(offer.getOwner().getProfilePic());
        }


        dto.setAverageRating(calculateAverageRating(offer.getReviews()));

        return dto;
    }

    public static Offer toEntity(OfferDTO dto, Category category) {
        Offer offer = new Offer();
        offer.setTitle(dto.getTitle());
        offer.setDescription(dto.getDescription());
        offer.setPrice(dto.getPrice() * 3);
        offer.setDeliveryTime(dto.getDeliveryTime());
        offer.setPaymentMethod(dto.getPaymentMethod());

        offer.setType(dto.getType());
        offer.setCategory(category);

        return offer;
    }

    private static Double calculateAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return null;
        }

        int total = 0;
        for (Review review : reviews) {
            total += review.getRating();
        }

        return total / (double) reviews.size();
    }

}

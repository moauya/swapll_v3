package com.swapll.gradu.service;

import com.swapll.gradu.model.Offer;
import com.swapll.gradu.model.Review;
import com.swapll.gradu.model.User;
import com.swapll.gradu.dto.ReviewDTO;
import com.swapll.gradu.repository.OfferRepository;
import com.swapll.gradu.repository.ReviewRepository;
import com.swapll.gradu.repository.UserRepository;
import com.swapll.gradu.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {


    private ReviewRepository reviewRepository;
    private OfferRepository offerRepository;
    private UserRepository userRepository;

    @Autowired
    public void setReviewRepository(ReviewRepository reviewRepository, OfferRepository offerRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;

    }

    public ReviewDTO addReview( ReviewDTO reviewDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User owner = userDetails.getUser();
        Offer offer = offerRepository.findById(reviewDto.getOfferId()).orElse(null);
        if (reviewRepository.existsByUserIdAndOfferId(owner.getId(), reviewDto.getOfferId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already reviewed this offer.");
        }
        Review review = new Review();

        review.setComment(reviewDto.getComment());
        review.setRating(reviewDto.getRating());
        review.setUser(owner);

            review.setOffer(offer);
            offer.addReview(review);

            reviewDto.setCreatedAt(review.getCreatedAt());
            reviewDto.setUserId(owner.getId());



        reviewRepository.save(review);

        reviewDto.setFirstName(owner.getFirstName());
        reviewDto.setLastName(owner.getLastName());
        reviewDto.setUserName(owner.getUserName());
        reviewDto.setProfilePicture(owner.getProfilePic());
        return reviewDto ;
    }
    public List<ReviewDTO> reviewsByOffer(int offerId) {
        List<Review> reviews = reviewRepository.findAllByOffer_Id(offerId);


        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }

        List<ReviewDTO> reviewDTOs = new ArrayList<>();

        for (Review review : reviews) {
            User user = review.getUser();
            ReviewDTO dto = new ReviewDTO();
            dto.setOfferId(offerId);
            dto.setUserId(review.getUser().getId());
            dto.setRating(review.getRating());
            dto.setComment(review.getComment());
            dto.setCreatedAt(review.getCreatedAt());
            reviewDTOs.add(dto);

            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setUserName(user.getUserName());
            dto.setProfilePicture(user.getProfilePic());
        }

        return reviewDTOs;
    }



    public boolean deleteReviewByOffer(int offerId, int reviewId) {
        boolean flag = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User owner = userDetails.getUser();

        Optional<Review> review = reviewRepository.findById(reviewId);
        Optional<Offer> offer = offerRepository.findById(offerId);

        if (review.isPresent() && offer.isPresent()) {
            owner.getReviews().remove(review.get());
            offer.get().getReviews().remove(review.get());

            if (review.get().getOffer().getId() == offerId && review.get().getUser().getId() == owner.getId()) {
                reviewRepository.delete(review.get());
                flag = true;
            }
        }

        return flag;
    }



}

package com.swapll.gradu.service;

import com.swapll.gradu.dto.SearchDTO;
import com.swapll.gradu.model.Category;
import com.swapll.gradu.model.Offer;
import com.swapll.gradu.model.User;
import com.swapll.gradu.dto.OfferDTO;
import com.swapll.gradu.dto.mappers.OfferMapper;
import com.swapll.gradu.repository.CategoryRepository;
import com.swapll.gradu.repository.OfferRepository;
import com.swapll.gradu.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OfferService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private S3Service s3Service;


    @Autowired
    public OfferService(OfferRepository repo) {

        offerRepository = repo;
    }


    public OfferDTO addOffer(OfferDTO dto, MultipartFile image) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User owner = userDetails.getUser();

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Offer offer = OfferMapper.toEntity(dto, category);
        offer.setOwner(owner);
        owner.addOffer(offer);
        category.addOffer(offer);

        if (image != null && !image.isEmpty()) {
            String fileName = "offer-images/" + UUID.randomUUID() + "-" + image.getOriginalFilename();
            String s3Url = s3Service.uploadFile(image, fileName);
            offer.setImage(s3Url);  // ✅ Store the S3 URL instead of byte[]
        }

        offerRepository.save(offer);
        return OfferMapper.toDTO(offer);
    }



    public List<OfferDTO> getAllOffersByCategoryId(int categoryId) {
        List<Offer> offers = offerRepository.findByCategoryId(categoryId);
        if (offers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }

        List<OfferDTO> offerDTOs = new ArrayList<>();
        for (Offer offer : offers) {
            offerDTOs.add(OfferMapper.toDTO(offer));
        }

        return offerDTOs;
    }

    public List<OfferDTO> getAllOffersByUserId(int ownerId) {
        List<Offer> offers = offerRepository.findByOwnerId(ownerId);
        if (offers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner  is not found");
        }

        List<OfferDTO> offerDTOs = new ArrayList<>();
        for (Offer offer : offers) {
            offerDTOs.add(OfferMapper.toDTO(offer));
        }

        return offerDTOs;
    }


    public OfferDTO updateOffer(OfferDTO dto, MultipartFile image) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User owner = userDetails.getUser();

        Offer offer = offerRepository.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("Offer not found"));

        if (!offer.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("You are not authorized to update this offer");
        }

        if (dto.getTitle() != null) offer.setTitle(dto.getTitle());
        if (dto.getDescription() != null) offer.setDescription(dto.getDescription());
        if (dto.getPrice() != 0) offer.setPrice(dto.getPrice() * 3);
        if (dto.getDeliveryTime() != 0) offer.setDeliveryTime(dto.getDeliveryTime());
        if (dto.getPaymentMethod() != null) offer.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getType() != null) offer.setType(dto.getType());

        if (dto.getCategoryId() != 0) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
            offer.setCategory(category);
        }

        if (image != null && !image.isEmpty()) {
            String fileName = "offer-images/" + UUID.randomUUID() + "-" + image.getOriginalFilename();
            String s3Url = s3Service.uploadFile(image, fileName);
            offer.setImage(s3Url); // ✅ New image URL
        }

        return OfferMapper.toDTO(offerRepository.save(offer));
    }







    public OfferDTO getOfferById(int id) {
        Offer offer=offerRepository.findById(id).orElseThrow(() -> new NoSuchElementException("there is no offer with this id"));


        return OfferMapper.toDTO(offer);

    }

    @Transactional
    public void deleteOffer(int offerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User currentUser = userDetails.getUser();

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new NoSuchElementException("Offer not found"));

        if (offer.getOwner() == null || offer.getOwner().getId() != currentUser.getId()) {
            throw new SecurityException("You are not authorized to delete this offer");
        }

        offerRepository.delete(offer);
    }

    public List<OfferDTO> searchOffers(SearchDTO searchDTO) {
        List<Offer> offers = offerRepository.search(searchDTO.getKeyword(), searchDTO.getCategoryId(), searchDTO.getMinPrice(), searchDTO.getMaxPrice(), searchDTO.getPaymentMethod());

        List<OfferDTO> dtos = new ArrayList<>();
        for (Offer offer : offers) {
            dtos.add(OfferMapper.toDTO(offer));
        }

        return dtos;
    }

    public List<OfferDTO> getRecentOffers() {
        List<Offer> offers = offerRepository.findTop10ByOrderByCreatedAtDesc();
        List<OfferDTO> dtoList = new ArrayList<>();
        for (Offer offer : offers) {
            dtoList.add(OfferMapper.toDTO(offer));
        }
        return dtoList;
    }

    public List<OfferDTO> getTopRatedOffers() {
        List<Offer> offers = offerRepository.findTopRatedOffers();
        List<OfferDTO> dtoList = new ArrayList<>();
        for (Offer offer : offers) {
            dtoList.add(OfferMapper.toDTO(offer));
        }
        return dtoList;
    }



}

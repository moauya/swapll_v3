package com.swapll.gradu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapll.gradu.Enum.PaymentMethod;
import com.swapll.gradu.dto.OfferDTO;
import com.swapll.gradu.dto.SearchDTO;
import com.swapll.gradu.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OfferController {

    @Autowired
    private OfferService offerService;



    @PostMapping(value = "/offer/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OfferDTO> addOffer(@RequestPart("offer") String offerJson,
                                             @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        OfferDTO offerDTO = mapper.readValue(offerJson, OfferDTO.class);

        OfferDTO savedOffer = offerService.addOffer(offerDTO, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOffer);
    }





    @GetMapping("/offers/category/{categoryId}")
    public List<OfferDTO> getOffersByCategoryId(@PathVariable int categoryId) {
        return offerService.getAllOffersByCategoryId(categoryId);
    }
    @GetMapping("/offers/user/{userId}")
    public List<OfferDTO> getOffersByUserId(@PathVariable int userId) {
       return offerService.getAllOffersByUserId(userId);
    }

    @PutMapping(value = "/offer/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OfferDTO> updateOffer(@RequestPart("offer") String offerJson,
                                                @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        OfferDTO offerDTO = mapper.readValue(offerJson, OfferDTO.class);

        OfferDTO updatedOffer = offerService.updateOffer(offerDTO, image);

        return ResponseEntity.ok(updatedOffer);
    }





    @GetMapping("/offer/{id}")
    public OfferDTO getOfferById(@PathVariable int id) {
        return offerService.getOfferById(id);
    }

    @DeleteMapping("/offer/{id}")
    public void deleteOffer(@PathVariable int id) {
        offerService.deleteOffer(id);
    }

    @PostMapping("/offers/search")
    public List<OfferDTO> searchOffers(@RequestBody SearchDTO searchDTO) {
        return offerService.searchOffers(searchDTO);
    }

    @GetMapping("offers/recent")
    public ResponseEntity<List<OfferDTO>> getRecentOffers() {
        return ResponseEntity.ok(offerService.getRecentOffers());
    }

    @GetMapping("offers/top-rated")
    public ResponseEntity<List<OfferDTO>> getTopRatedOffers() {
        return ResponseEntity.ok(offerService.getTopRatedOffers());
    }






}

package com.swapll.gradu.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.swapll.gradu.dto.UserDTO;
import com.swapll.gradu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api")
@RestController
public class UserController {
     private UserService userService;



     @Autowired
     public UserController(UserService userService) {
          this.userService = userService;
     }

     @GetMapping("/user/{userId}")
     public UserDTO getUserById(@PathVariable int userId ){

        return userService.getUserById(userId);
     }

     @PutMapping(value = "/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
     public UserDTO updateUser(
             @RequestPart("user") String userJson,
             @RequestPart(value = "profilePic", required = false) MultipartFile profilePic
     ) throws JsonProcessingException {

          ObjectMapper mapper = new ObjectMapper();
          UserDTO userDTO = mapper.readValue(userJson, UserDTO.class);

          return userService.updateUser(userDTO, profilePic);
     }



     @PostMapping("/user/change-password")
     public ResponseEntity<String> changePassword(
             @RequestParam String oldPassword,
             @RequestParam String newPassword) {

          userService.changePassword(oldPassword, newPassword);
          return ResponseEntity.ok("Password changed successfully");
     }

     @GetMapping("/user/myinfo")
     public UserDTO getUserInfo(){

        return  userService.getUserInfo();
     }


     @GetMapping("/user/ref/{refCode}")
     public String getUserNameByRefCode(@PathVariable String refCode){

          return userService.getUserNameByRefCode(refCode);
     }



     }








package com.swapll.gradu.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapll.gradu.dto.EmailRequest;
import com.swapll.gradu.dto.ResetRequest;
import com.swapll.gradu.dto.UserDTO;
import com.swapll.gradu.model.User;
import com.swapll.gradu.dto.login.LoginRequest;
import com.swapll.gradu.dto.login.LoginResponse;
import com.swapll.gradu.dto.login.RegisterResponse;
import com.swapll.gradu.dto.mappers.UserMapper;
import com.swapll.gradu.security.CustomUserDetails;
import com.swapll.gradu.security.JwtUtil;
import com.swapll.gradu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;




    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestPart("user") String userJson,
                                                     @RequestPart(value = "profilePic", required = false) MultipartFile profilePic) throws JsonProcessingException {

        // by yazan
        ObjectMapper mapper = new ObjectMapper();
        UserDTO userDTO = mapper.readValue(userJson, UserDTO.class);

        UserDTO registeredUser = userService.registerUser(userDTO, profilePic);

        User user = userService.getUserByEmailOrUsername(registeredUser.getEmail());
        String token = jwtUtil.generateToken(new CustomUserDetails(user));


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(token, registeredUser));
    }



    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println("Attempting to authenticate user: " + request.getUsernameOrEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed for: " + request.getUsernameOrEmail());
            throw new UsernameNotFoundException("Invalid credentials");
        }

        User user = userService.getUserByEmailOrUsername(request.getUsernameOrEmail());

        String jwt = jwtUtil.generateToken(new CustomUserDetails(user));
        System.out.println("Generated JWT: " + jwt);

        return ResponseEntity.ok(new LoginResponse(jwt));
    }

    @PostMapping("/request-reset")
    public ResponseEntity<String> requestReset(@RequestBody EmailRequest email) {
        userService.sendResetCode(email.getEmail());
        return ResponseEntity.ok("Reset code sent to email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestBody ResetRequest resetRequest) {
        userService.resetPassword(resetRequest);
        return ResponseEntity.ok("Password reset successful");
    }




}




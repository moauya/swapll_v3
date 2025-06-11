package com.swapll.gradu.service;

import com.swapll.gradu.model.User;
import com.swapll.gradu.dto.ResetRequest;
import com.swapll.gradu.dto.UserDTO;
import com.swapll.gradu.dto.mappers.UserMapper;
import com.swapll.gradu.repository.UserRepository;
import com.swapll.gradu.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;



    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public UserDTO registerUser(UserDTO userDTO, MultipartFile profilePic) {
        if (userRepository.existsByUserNameIgnoreCase(userDTO.getUserName())) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (userRepository.existsByEmailIgnoreCase(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = UserMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                user.setProfilePic(profilePic.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile picture", e);
            }
        }

        if (userDTO.getReferralCode() != null &&
                userRepository.existsByMyReferralCodeIgnoreCase(userDTO.getReferralCode())) {

            user.setBalance(user.getBalance() + 10);
            User referrer = userRepository.findByMyReferralCodeIgnoreCase(userDTO.getReferralCode())
                    .orElseThrow(() -> new RuntimeException("Referral code not found"));
            referrer.setBalance(referrer.getBalance() + 3);
            userRepository.save(referrer);
        } else {
            user.setReferralCode(null);
        }

        userRepository.save(user);
        return UserMapper.toDTO(user);
    }


    @Transactional
    public UserDTO updateUser(UserDTO updatedUserDTO, MultipartFile profilePic) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User owner = userDetails.getUser();

        if (updatedUserDTO.getUserName() != null &&
                !updatedUserDTO.getUserName().equals(owner.getUserName())) {
            if (userRepository.existsByUserNameIgnoreCase(updatedUserDTO.getUserName())) {
                throw new IllegalArgumentException("Username already taken");
            }
            owner.setUserName(updatedUserDTO.getUserName());
        }

        if (updatedUserDTO.getEmail() != null &&
                !updatedUserDTO.getEmail().equals(owner.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(updatedUserDTO.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            owner.setEmail(updatedUserDTO.getEmail());
        }

        if (updatedUserDTO.getFirstName() != null) owner.setFirstName(updatedUserDTO.getFirstName());
        if (updatedUserDTO.getLastName() != null) owner.setLastName(updatedUserDTO.getLastName());
        if (updatedUserDTO.getPhone() != null) owner.setPhone(updatedUserDTO.getPhone());
        if (updatedUserDTO.getAddress() != null) owner.setAddress(updatedUserDTO.getAddress());
        if (updatedUserDTO.getReferralCode() != null) owner.setReferralCode(updatedUserDTO.getReferralCode());
        if (updatedUserDTO.getBio() != null) owner.setBio(updatedUserDTO.getBio());

        if (profilePic != null && !profilePic.isEmpty()) {
            try {
                owner.setProfilePic(profilePic.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to update profile picture", e);
            }
        }

        User updatedOwner = userRepository.save(owner);
        return UserMapper.toDTO(updatedOwner);
    }


    public User getUserByEmailOrUsername(String username) {
        return userRepository.findByUserNameIgnoreCaseOrEmailIgnoreCase(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    public UserDTO getUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return UserMapper.toDTO(user);
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserDTO getUserInfo(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User owner = userDetails.getUser();
        UserDTO dto=UserMapper.toDTO(owner);

        return dto;
    }

    public byte[] getUserProfilePic(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getProfilePic();
    }

    public String getUserNameByRefCode(String refCode){
        Optional<User> user=userRepository.findByMyReferralCodeIgnoreCase(refCode);
        if (user.isPresent()){
            return user.get().getUserName();
        }

        return "Oops! Referral code not recognized";

    }




    public void sendResetCode(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String code = String.valueOf(100000 + new Random().nextInt(900000));
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(2));
        userRepository.save(user);

        String htmlContent = """
        <html>
          <body style="font-family: Arial, sans-serif; padding: 20px;">
            <h2 style="color: #2e6c80;">Password Reset Code</h2>
            <p>Hello,</p>
            <p>Your verification code is:</p>
            <div style="font-size: 24px; font-weight: bold; color: #ff6600;">%s</div>
            <p>Please use this code to reset your password in the app.</p>
            <br>
            <p style="font-size: 12px; color: gray;">If you did not request this, please ignore this email.</p>
          </body>
        </html>
        """.formatted(code);  // Insert the actual code here

        emailService.sendHtmlEmail(email, "Reset Code", htmlContent);
    }


    public void resetPassword(ResetRequest resetRequest) {
        User user = userRepository.findByEmailIgnoreCase(resetRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getResetCode() == null ||
                !user.getResetCode().equals(resetRequest.getCode()) ||
                user.getResetCodeExpiry() == null ||
                user.getResetCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired code");
        }

        user.setPassword(new BCryptPasswordEncoder().encode(resetRequest.getNewPassword()));
        user.setResetCode(null); // clear after use
        user.setResetCodeExpiry(null);
        userRepository.save(user);
    }
}



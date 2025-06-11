package com.swapll.gradu.dto.mappers;

import com.swapll.gradu.model.User;
import com.swapll.gradu.dto.UserDTO;

public class UserMapper {

    public static User toEntity(UserDTO dto) {
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setBio(dto.getBio());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setReferralCode(dto.getReferralCode());
        user.setPassword(dto.getPassword());



        return user;
    }

    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setReferralCode(user.getReferralCode());
        dto.setMyReferralCode(user.getMyReferralCode());
        dto.setBio(user.getBio());
        dto.setBalance(user.getBalance());

        dto.setProfilePic(user.getProfilePic());
        return dto;
    }
}

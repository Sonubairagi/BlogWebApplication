package com.blogapp.service;

import com.blogapp.payload.LoginDto;
import com.blogapp.payload.UserDetailsDto;
import com.blogapp.payload.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface UserService {
    UserDetailsDto userRegister(UserDto userDto, MultipartFile profileImage);
    String userAuthentication(LoginDto loginDto);
    String deleteUserDetails(Long userId);
    UserDetailsDto updateUserDetails(Long userId, UserDto userDto,MultipartFile profileImage);
    UserDetailsDto getUserByUsername(Long userId);
    List<UserDetailsDto> listOfUsers();
    boolean verifyUser(String userEmailId);
}

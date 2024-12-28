package com.xplore.service;

import com.xplore.payload.LoginDto;
import com.xplore.payload.UserDetailsDto;
import com.xplore.payload.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface UserService {
    UserDetailsDto userRegister(UserDto userDto, MultipartFile profileImage);
    String userAuthentication(LoginDto loginDto);
    String deleteUserDetails(Long userId);
    UserDetailsDto updateUserDetails(Long userId, UserDto userDto,MultipartFile profileImage);
    UserDetailsDto getUserById(Long userId);
    List<UserDetailsDto> listOfUsers();
    boolean verifyUser(String userEmailId);
}

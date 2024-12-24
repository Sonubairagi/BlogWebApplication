package com.blogapp.controller;

import com.blogapp.payload.LoginDto;
import com.blogapp.payload.UserDetailsDto;
import com.blogapp.payload.UserDto;
import com.blogapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.lang.String;

@RestController
@RequestMapping("api/auth/user")
public class UserController {

    private UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    //http://localhost:8080/api/auth/user/sign-up
    @PostMapping("/sign-up")
    public ResponseEntity<UserDetailsDto> userRegistration(
            @Valid @RequestPart("userDto") UserDto userDto,
            @RequestPart("profileImage") MultipartFile profileImage
    ){
        UserDetailsDto userDetails = userService.userRegister(userDto,profileImage);
        return new ResponseEntity<>(userDetails, HttpStatus.CREATED);
    }

    //http://localhost:8080/api/auth/user/verify?userEmailId=emailId
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(
            @RequestParam("userEmailId")String userEmailId
    ){
        boolean isVerified = userService.verifyUser(userEmailId);

        if (isVerified) {
            return ResponseEntity.ok("User successfully verified!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user email ID.");
        }
    }

    //http://localhost:8080/api/auth/user/sign-in
    @PostMapping("/sign-in")
    public ResponseEntity<String> userAuthentication(
            @RequestBody LoginDto LoginDto
    ){
        String userVerify = userService.userAuthentication(LoginDto);
        return new ResponseEntity<>(userVerify, HttpStatus.OK);
    }

    //http://localhost:8080/api/auth/user/deleteUser/{userId}
    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<String> userDelete(
            @PathVariable Long userId
    ){
        String deleteUser = userService.deleteUserDetails(userId);
        return new ResponseEntity<>(deleteUser, HttpStatus.OK);
    }

    //http://localhost:8080/api/auth/user/updateUser/{userId}
    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<UserDetailsDto> userUpdate(
            @PathVariable Long userId,
            @RequestPart("userDto") UserDto userDto,
            @RequestPart("file") MultipartFile profileImage
    ){
        UserDetailsDto updateUser = userService.updateUserDetails(userId,userDto,profileImage);
        return new ResponseEntity<>(updateUser, HttpStatus.OK);
    }

    //http://localhost:8080/api/auth/user/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailsDto> getUserById(
            @PathVariable Long userId
    ){
        UserDetailsDto user = userService.getUserByUsername(userId);
        return new ResponseEntity<>(user,HttpStatus.OK);
    }

    //http://localhost:8080/api/auth/user
    @GetMapping
    public ResponseEntity<List<UserDetailsDto>> getUsers(){
        List<UserDetailsDto> listOfUsers = userService.listOfUsers();
        return new ResponseEntity<>(listOfUsers,HttpStatus.OK);
    }


}

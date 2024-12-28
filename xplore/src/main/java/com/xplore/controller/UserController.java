package com.xplore.controller;

import com.xplore.exception.ImageUploadException;
import com.xplore.exception.ImagesLimitExceedException;
import com.xplore.payload.LoginDto;
import com.xplore.payload.UserDetailsDto;
import com.xplore.payload.UserDto;
import com.xplore.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.lang.String;

@RestController
@RequestMapping("api/auth/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    //http://localhost:8080/api/auth/user/sign-up
    @PostMapping("/sign-up")
    public ResponseEntity<UserDetailsDto> userRegistration(
            @Valid @RequestPart("userDto") UserDto userDto,
            @RequestPart("profileImage") MultipartFile profileImage
    ){
        logger.info("Entering getUser with User: {}", userDto.getUserName());
        UserDetailsDto userDetails = null;

        if(!profileImage.isEmpty() || !profileImage.getOriginalFilename().isBlank()){
            logger.info("Successfully retrieved profile image: {}", profileImage.getName());
            userDetails = userService.userRegister(userDto,profileImage);
            if(userDetails != null){
                return new ResponseEntity<>(userDetails, HttpStatus.CREATED);
            }
            return new ResponseEntity<>(userDetails, HttpStatus.BAD_REQUEST);
        }else{
            ImageUploadException e = new ImageUploadException("Image is not getting!");
            logger.error("Failed! to retrieve user profile image: {} : {}",e.getMessage(),e.getStackTrace());
            return new ResponseEntity<>(userDetails, HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/auth/user/verify?userEmailId=emailId
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(
            @RequestParam("userEmailId")String userEmailId
    ){
        boolean isVerified = false;

        logger.info("Getting User verify email: {}", userEmailId);

        isVerified = userService.verifyUser(userEmailId);

        if (isVerified) {
            logger.info("Successfully retrieved user verify email: {} is verified: {}", userEmailId,isVerified);
            return new ResponseEntity<>("User successfully verified!",HttpStatus.OK);
        } else {
            logger.error("Failed to User verification: {}", isVerified);
            return new ResponseEntity<>("Invalid user email ID.",HttpStatus.BAD_REQUEST);
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
        if(userId != null){
            logger.info("Successfully getting user id: {}",userId);
            String deleteUser = userService.deleteUserDetails(userId);
            return new ResponseEntity<>(deleteUser, HttpStatus.OK);
        }else {
            logger.error("Failed! User id is not getting: {}", userId);
            return new ResponseEntity<>("User id not getting...", HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/auth/user/updateUser/{userId}
    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<UserDetailsDto> userUpdate(
            @PathVariable Long userId,
            @RequestPart("userDto") UserDto userDto,
            @RequestPart("file") MultipartFile profileImage
    ){
        UserDetailsDto updateUser = null;
        logger.info("Update user information: {}", userDto.getUserName());
        if(userId != null){
            if(!profileImage.isEmpty() || !profileImage.getOriginalFilename().isBlank()){
                logger.info("Success! Updated User id is: {}",userId);
                updateUser = userService.updateUserDetails(userId,userDto,profileImage);
                if(updateUser != null){
                    return new ResponseEntity<>(updateUser, HttpStatus.OK);
                }
                return new ResponseEntity<>(updateUser, HttpStatus.BAD_REQUEST);
            }
            logger.warn("User profile not found!");
            return new ResponseEntity<>(updateUser, HttpStatus.BAD_REQUEST);
        }else{
            logger.error("Failed! User id is not correct: {}", userId);
            return new ResponseEntity<>(updateUser, HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/auth/user/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailsDto> getUserById(
            @PathVariable Long userId
    ){
        UserDetailsDto user = null;
        if(userId != null){
            logger.info("Success! Get the user id is: {}",userId);
            user = userService.getUserById(userId);
            if (user != null){
                return new ResponseEntity<>(user,HttpStatus.OK);
            }
            logger.warn("User not found! By Id: {}",userId);
            return new ResponseEntity<>(user,HttpStatus.BAD_REQUEST);
        }else{
            logger.error("Failed! Get the User id is not correct: {}", userId);
            return new ResponseEntity<>(user,HttpStatus.BAD_REQUEST);
        }
    }

    //http://localhost:8080/api/auth/user
    @GetMapping
    public ResponseEntity<List<UserDetailsDto>> getUsers(){
        List<UserDetailsDto> listOfUsers = null;
        try{
            listOfUsers = userService.listOfUsers();
            if(listOfUsers != null){
                logger.info("Getting all the users object: {}",listOfUsers.size());
                return new ResponseEntity<>(listOfUsers,HttpStatus.OK);
            }
        }catch (Exception e){
            logger.error("User's not found! : {}",e.getMessage());
        }
        return new ResponseEntity<>(listOfUsers,HttpStatus.BAD_REQUEST);
    }


}

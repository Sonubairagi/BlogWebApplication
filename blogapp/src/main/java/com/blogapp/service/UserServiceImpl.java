package com.blogapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.blogapp.entity.Address;
import com.blogapp.entity.User;
import com.blogapp.exception.UserAlreadyExistsException;
import com.blogapp.exception.UserNotFoundException;
import com.blogapp.payload.LoginDto;
import com.blogapp.payload.UserDetailsDto;
import com.blogapp.payload.UserDto;
import com.blogapp.repository.AddressRepository;
import com.blogapp.repository.UserRepository;
import com.blogapp.util.EmailSenderService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonS3 client;

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public UserDetailsDto mapToDto(User user){
        UserDetailsDto userDto = new UserDetailsDto();
        userDto.setUserName(user.getUserName());
        userDto.setEmail(user.getEmail());
        userDto.setMobile(user.getMobile());
        userDto.setAddress(user.getAddress());
        userDto.setCreateAt(user.getCreateAt());
        userDto.setProfileImagePath(user.getProfileImagePath());
        return userDto;
    }

    public User mapToEntity(UserDto userDto){
        return modelMapper.map(userDto,User.class);
    }

    public Address mapToAddress(UserDto userDto){
        Address address = new Address();
        address.setAreaName(userDto.getAddress().getAreaName());
        address.setCityName(userDto.getAddress().getCityName());
        address.setPinCode(userDto.getAddress().getPinCode());
        address.setStateName(userDto.getAddress().getStateName());
        address.setCountryName(userDto.getAddress().getCountryName());
        return address;
    }

    public String emailFormatting(String username,String verificationLink){
        return String.format(
                "<div style='width: 100%%; height: 100%%; display: flex; justify-content: center; align-items: center;'>" +
                        "<div style='text-align: center; padding: 20px; border: 1px solid #4CAF50; border-radius: 10px;'>" +
                        "<p style='font-size: 16px; font-weight: bold;'>Hello %s,</p>" +
                        "<p style='font-size: 16px;'>Thank you for registering! Please click the button below to verify your email address:</p>" +
                        "<a href='%s' style='background-color: lightgreen; color: #000; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; border: 2px solid lightgreen; transition: all 0.3s ease;'>" +
                        "Verify Email" +
                        "</a>" +
                        "<p style='font-size: 16px;'>If you did not register, please ignore this email.</p>" +
                        "</div>" +
                        "</div>",
                username, verificationLink);
    }

    @Override
    public UserDetailsDto userRegister(UserDto userDto,MultipartFile profileImage){
        logger.debug("Processing user with user object: {}", userDto);
        User user = null;
        String imageUrl = null;
        String subject = null;
        String verificationLink = null;
        String body = null;
        User saved = null;
        try{
            Optional<User> opUser = userRepository.findByUserName(userDto.getUserName());
            if(opUser.isPresent()){
                logger.warn("Warning! User Already Exists: {}", opUser.get().getUserName());
                throw new UserAlreadyExistsException("User with username " + userDto.getUserName() + " already exists.");
            }

            logger.info("Successfully processed user: {}", opUser);

            user = mapToEntity(userDto);
            user.setRole("ROLE_USER");

            //password encryption
            //String password = userDto.getPassword();

            user.setCreateAt(LocalDateTime.now().withNano(0));
            user.setUpdateAt(LocalDateTime.now().withNano(0));

            //image store in db and local
            //String originalFilename = System.currentTimeMillis()+""+profileImage.getOriginalFilename();
            //Path fileNameAndPath = Paths.get(uploadDir+"user_profile",originalFilename);
            //try {
            //     Files.write(fileNameAndPath,profileImage.getBytes());
            //     user.setProfileImagePath(originalFilename);
            //}catch (Exception e){
            //     e.printStackTrace();
            //}

            //image upload in aws cloud
            if(profileImage != null && !profileImage.isEmpty()){
                imageUrl = awsS3Service.uploadImage(profileImage);
                logger.info("Successfully processed user profile image: {}", profileImage);
                user.setProfileImagePath(imageUrl);
            }else{
               logger.warn("User profile image is not getting: {}",profileImage);
            }

            user.setAddress(mapToAddress(userDto));

            verificationLink = "http://192.168.31.94:8080/api/auth/user/verify?userEmailId=" + user.getEmail();
            body = emailFormatting(userDto.getUserName(), verificationLink);

            saved = userRepository.save(user);
            logger.info("User registration Successfully!");
            if(saved != null){
                try {
                    subject = "Email Verification";
                    logger.info("User registration email sending...");
                    emailSenderService.sendHtmlEmail(user.getEmail(),subject,body); //MessagingException
                } catch (Exception e) {
                    logger.error("User registration failed! not sending email: {} : {}",e.getMessage(),e.getStackTrace());
                }
                return mapToDto(saved);
            }
        }catch (Exception e){
            logger.error("User Details :- {} : {}",e.getMessage(),e.getStackTrace());
        }
        return null;
    }

    public boolean verifyUser(String userEmailId) {
        logger.debug("Processing user verification {}", userEmailId);
        Optional<User> optionalUser = null;
        try{
            optionalUser = userRepository.findByEmail(userEmailId);
            if (optionalUser.isPresent()) {
                logger.info("Successfully processed user by: {}", optionalUser.get().getUserName());
                User user = optionalUser.get();
                //user.setRole(user.getRole());
                user.setCreateAt(user.getCreateAt());
                user.setUpdateAt(LocalDateTime.now().withNano(0));
                user.setAddress(user.getAddress());
                user.setVerified(true); // Mark the user as verified
                User verify = userRepository.save(user);
                if(verify != null){
                    logger.info("Success! User verification is done");
                    return true;
                }
            }
        }catch (Exception e){
            logger.error("Failed! User verification: {} : {}",e.getMessage(),e.getStackTrace());
        }
        return false;
    }

    @Override
    public String userAuthentication(LoginDto loginDto) {
        Optional<User> opUser = userRepository.findByUserName(loginDto.getUserName());
        if(opUser.isPresent()){
            User user = opUser.get();
            if(loginDto.getPassword().equals(user.getPassword())){
                return "User is logged in...";
            }else{
                return "invalid username/password";
            }
        }
        return "invalid username/password";
    }

    @Override
    public String deleteUserDetails(Long userId) {
        logger.debug("Processing user with ID: {}", userId);
        boolean deleteImage = false;
        try{
            Optional<User> opUser = userRepository.findById(userId);
            if(opUser.isPresent()){
                logger.info("Successfully processed user!");
                deleteImage = awsS3Service.deleteImage(opUser.get().getProfileImagePath());
                if(deleteImage){
                    userRepository.deleteById(userId);
                    logger.info("Successfully processed user is deleted!");
                    return "User is deleted by user id : "+userId;
                }
            }
        }catch (Exception e){
            logger.error("User not found: {}",e.getMessage());
            throw new UserNotFoundException("user is not found by userId " + userId);
        }
        return "user is not found!";
    }

    @Override
    public UserDetailsDto updateUserDetails(Long userId, UserDto userDto,MultipartFile profileImage) {
        logger.debug("Processing user with id: {}", userId);
        Optional<User> opUser;
        boolean deleteImage = false;
        String newImageUrl = null;
        try{
            opUser = userRepository.findById(userId);
            if(opUser.isPresent()){
                logger.info("Successfully processed user");
                User user = opUser.get();
                user.setUserName(userDto.getUserName());
                user.setMobile(userDto.getMobile());
                user.setRole(user.getRole());
                user.setCreateAt(user.getCreateAt());
                user.setUpdateAt(LocalDateTime.now().withNano(0));

                user.setAddress(mapToAddress(userDto));

                if (profileImage != null && !profileImage.isEmpty()) {
                    deleteImage = awsS3Service.deleteImage(user.getProfileImagePath());
                    if(deleteImage){
                        newImageUrl = awsS3Service.uploadImage(profileImage);
                        user.setProfileImagePath(newImageUrl);
                        logger.info("Success! processed user profile image: {}", profileImage);
                    }
                }
                //password not update
                //email not update

                User saved = userRepository.save(user);
                logger.info("Success! update user details");
                return mapToDto(saved);
            }
        }catch (Exception e){
            logger.error("User details not updated: {} : {}",e.getMessage(),e.getStackTrace());
        }
        return null;
    }

    @Override
    public UserDetailsDto getUserById(Long userId) {
        logger.debug("Processing get user with ID: {}", userId);
        Optional<User> opUser = userRepository.findById(userId);
        if(opUser.isPresent()){
            logger.info("Success! User object is present");
            return mapToDto(opUser.get());
        }
        logger.warn("user not found by id: {}",userId);
        return null;
    }

    @Override
    public List<UserDetailsDto> listOfUsers() {
        logger.debug("Processing get all users..");
        List<User> userList = userRepository.findAll();
        return userList.stream().map((element) -> modelMapper.map(element, UserDetailsDto.class)).collect(Collectors.toList());
    }
}
package com.blogapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.blogapp.entity.Address;
import com.blogapp.entity.User;
import com.blogapp.exception.UserAlreadyExistsException;
import com.blogapp.payload.LoginDto;
import com.blogapp.payload.UserDetailsDto;
import com.blogapp.payload.UserDto;
import com.blogapp.repository.AddressRepository;
import com.blogapp.repository.UserRepository;
import com.blogapp.util.EmailSenderService;
import org.modelmapper.ModelMapper;
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

    @Override
    public UserDetailsDto userRegister(UserDto userDto,MultipartFile profileImage){

        Optional<User> opUser = userRepository.findByUserName(userDto.getUserName());
        if(opUser.isPresent()){
            throw new UserAlreadyExistsException("User with username " + userDto.getUserName() + " already exists.");
        }
        User user = mapToEntity(userDto);
        user.setRole("ROLE_USER");

        //password encryption
        //String password = userDto.getPassword();

        user.setCreateAt(LocalDateTime.now().withNano(0));
        user.setUpdateAt(LocalDateTime.now().withNano(0));

        //image store in db and local
//        String originalFilename = System.currentTimeMillis()+""+profileImage.getOriginalFilename();
//        Path fileNameAndPath = Paths.get(uploadDir+"user_profile",originalFilename);
//        try {
//            Files.write(fileNameAndPath,profileImage.getBytes());
//            user.setProfileImagePath(originalFilename);
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        //image upload in aws cloud
        if(profileImage != null && !profileImage.isEmpty()){
            String imageUrl = awsS3Service.uploadImage(profileImage);
            user.setProfileImagePath(imageUrl);
        }

        Address address = new Address();
        address.setAreaName(userDto.getAddress().getAreaName());
        address.setCityName(userDto.getAddress().getCityName());
        address.setPinCode(userDto.getAddress().getPinCode());
        address.setStateName(userDto.getAddress().getStateName());
        address.setCountryName(userDto.getAddress().getCountryName());
        user.setAddress(address);

        String subject = "Email Verification";
        String verificationLink = "http://192.168.31.94:8080/api/auth/user/verify?userEmailId=" + user.getEmail();

        String body = String.format(
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
                userDto.getUserName(), verificationLink);

        User saved = userRepository.save(user);
        if(saved!=null){
            try {
                System.out.println("email sending");
//                emailSenderService.sendHtmlEmail(user.getEmail(),subject,body); //MessagingException
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return mapToDto(saved);
    }

    //only get the image url you can use this method
//    public String preSignedUrl(String fileName){
//        Date expirationDate = new Date();
//
//        Long time = expirationDate.getTime();
//        int hour = 1;
//        time = time + hour * 60 * 60;
//
//        expirationDate.setTime(time);
//
//        GeneratePresignedUrlRequest generatePresignedUrlRequest =
//                new GeneratePresignedUrlRequest(
//                        bucketName,
//                        fileName)
//                        .withMethod(HttpMethod.GET)
//                        .withExpiration(expirationDate);
//        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
//        return url.toString();
//    }


    public boolean verifyUser(String userEmailId) {
        Optional<User> optionalUser = userRepository.findByEmail(userEmailId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
//            user.setRole(user.getRole());
            user.setCreateAt(user.getCreateAt());
            user.setUpdateAt(LocalDateTime.now().withNano(0));
            user.setAddress(user.getAddress());
            user.setVerified(true); // Mark the user as verified
            userRepository.save(user);
            return true;
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
        Optional<User> opUser = userRepository.findById(userId);
        if(opUser.isPresent()){
            boolean deleteImage = awsS3Service.deleteImage(opUser.get().getProfileImagePath());
            if(deleteImage){
                userRepository.deleteById(userId);
                return "User is deleted by user id : "+userId;
            }
        }
        return "user is not found!";
    }

    @Override
    public UserDetailsDto updateUserDetails(Long userId, UserDto userDto,MultipartFile profileImage) {
        Optional<User> opUser = userRepository.findById(userId);
        if(opUser.isPresent()){
            User user = opUser.get();
            user.setUserName(userDto.getUserName());
            user.setMobile(userDto.getMobile());
            user.setRole(user.getRole());
            user.setCreateAt(user.getCreateAt());
            user.setUpdateAt(LocalDateTime.now().withNano(0));

            Address address = new Address();
            address.setId(user.getAddress().getId());
            address.setAreaName(userDto.getAddress().getAreaName());
            address.setCityName(userDto.getAddress().getCityName());
            address.setPinCode(userDto.getAddress().getPinCode());
            address.setStateName(userDto.getAddress().getStateName());
            address.setCountryName(userDto.getAddress().getCountryName());

            user.setAddress(address);

            if (profileImage != null && !profileImage.isEmpty()) {
                boolean deleteImage = awsS3Service.deleteImage(user.getProfileImagePath());
                if(deleteImage){
                    String newImageUrl = awsS3Service.uploadImage(profileImage);
                    user.setProfileImagePath(newImageUrl);
                }
            }
            //password not update
            //email not update
            User saved = userRepository.save(user);
            return mapToDto(saved);
        }
        return null;
    }

    @Override
    public UserDetailsDto getUserByUsername(Long userId) {
        Optional<User> opUser = userRepository.findById(userId);
        if(opUser.isPresent()){
            return mapToDto(opUser.get());
        }
        return null;
    }

    @Override
    public List<UserDetailsDto> listOfUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map((element) -> modelMapper.map(element, UserDetailsDto.class)).collect(Collectors.toList());
    }
}

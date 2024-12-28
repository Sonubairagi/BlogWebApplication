package com.xplore.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.xplore.entity.Category;
import com.xplore.entity.Post;
import com.xplore.entity.User;
import com.xplore.exception.CategoryNotFoundException;
import com.xplore.exception.PostNotFoundException;
import com.xplore.exception.UserNotFoundException;
import com.xplore.payload.PostDetailsDto;
import com.xplore.payload.PostDto;
import com.xplore.repository.CategoryRepository;
import com.xplore.repository.PostRepository;
import com.xplore.repository.UserRepository;
import com.xplore.util.EmailService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostServiceImpl.class);

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public PostDetailsDto mapToDto(Post post) {
        PostDetailsDto postDto = new PostDetailsDto();
        postDto.setTitle(post.getTitle());
        postDto.setDescription(post.getDescription());
        postDto.setUpdateAt(post.getUpdateAt());
        postDto.setCategory(post.getCategory());
        postDto.setUser(post.getUser());
        postDto.setPostImagesPath(post.getPostImagesPath());
        return postDto;
    }

    public Post mapToEntity(PostDto postDto) {
        return modelMapper.map(postDto, Post.class);
    }

    public String mailFormat(String userName,String postTitle,LocalDateTime createTime){
        return String.format(
                "Hello %s,\n\n" +
                        "Congratulations! Your post titled \"%s\" has been successfully created.\n\n" +
                        "Thank you for contributing to our platform. We're excited to see your content making an impact!\n\n" +
                        "Post is create by this time : %s\n\n" +
                        "Best Regards,\n" +
                        "[Application : Blog App] Team",
                userName, postTitle, createTime
        );
    }

    @Override
    public PostDetailsDto addPost(PostDto postDto, List<MultipartFile> postImages) {
        Post post = null;
        User user;
        Category category;
        List<String> imagePaths = null;
        try {
            logger.info("Starting adding post functionality....");
            post = mapToEntity(postDto);

            user = userRepository.findById(postDto.getUserId()).orElseThrow(
                    ()-> new UserNotFoundException("User not found! By Id: "+postDto.getUserId())
            );
            if (user != null) {
                logger.info("Success! Post was founded! by id: {}",user.getId());
                post.setUser(user);
            }

            category = categoryRepository.findById(postDto.getCategoryId()).orElseThrow(
                    ()-> new CategoryNotFoundException("Category not found! By Id: "+postDto.getCategoryId())
            );
            if (category != null) {
                logger.info("Success! Category is founded! by id: {}",category.getId());
                post.setCategory(category);
            }

            imagePaths = new ArrayList<>();
            for (MultipartFile image : postImages) {
                if (!image.isEmpty()) {
                    String originalFilePath = image.getOriginalFilename();

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(image.getSize());

                    if(originalFilePath != null){
                        String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));
                        amazonS3.putObject(bucketName, fileName, image.getInputStream(), metadata);
                        URL url = amazonS3.getUrl(bucketName, fileName);
                        imagePaths.add(url.toString());
                    }
                }
            }

            post.setPostImagesPath(imagePaths);
            post.setCreateAt(LocalDateTime.now().withNano(0));
            post.setUpdateAt(LocalDateTime.now().withNano(0));

            Post saved = postRepository.save(post);
            logger.info("Success! Post saved successfully with ID: {}", saved.getId());

            if (saved != null) {
                LocalDateTime createAt = post.getCreateAt();
                LocalDate date = createAt.toLocalDate();
                String userName = post.getUser().getUserName();
                String postTitle = post.getTitle();
                LocalDateTime createTime = post.getCreateAt();

                String subject = "Post is create by " + post.getUser().getUserName() + " on this time : "+ date;
                String message = mailFormat(userName, postTitle, createTime);

                emailService.sendMail(
                        saved.getUser().getEmail(),
                        subject,
                        message
                );
                logger.info("Success! Mail sending by this user to create the post: {}",userName);
            }
            return mapToDto(saved);
        } catch (Exception e) {
            logger.error("Error occurred while adding post: {}", e.getMessage(), e);
        } finally {
            post = null;
            user = null;
            category = null;
            imagePaths = null;
            logger.info("Adding post functionality completed");
        }
        return null;
    }

    @Override
    public String deletePostDetails(Long postId) {
        Post post = null;
        List<String> postImagesPath = null;
        boolean deleteImage = false;
        try {
            logger.info("Starting delete post details for postId: {}", postId);
            post = postRepository.findById(postId).orElseThrow(
                    ()-> new PostNotFoundException("Post not found! By Id: " + postId)
            );
            if (post != null) {
                logger.info("Success! Post was founded! by id: {}",postId);

                postImagesPath = post.getPostImagesPath();

                for (String filePath : postImagesPath) {
                    deleteImage = awsS3Service.deleteImage(filePath);
                }

                if (deleteImage) {
                    logger.info("Image was deleted in cloud!");
                    postRepository.deleteById(postId);
                    logger.info("Post deleted successfully with ID: {}", postId);
                    return "Post is deleted by post id : " + postId;
                }
            }
            logger.warn("Warning! Post was not founded! by id: {}",postId);
        } catch (Exception e) {
            logger.error("Error occurred while deleting post: {}", e.getMessage(), e);
        } finally {
            post = null;
            postImagesPath = null;
            logger.info("deleting post details functionality completed");
        }
        return "Post is not found! By id: " + postId;
    }

    @Override
    public PostDetailsDto updatePost(Long postId, PostDto postDto, List<MultipartFile> postImages) {
        Post post = null;
        User user;
        Category category;
        List<String> imagePaths = null;
        boolean deleteImage = false;
        Post saved = null;

        try {
            logger.info("Starting update post functionality for postId: {}", postId);
            post = postRepository.findById(postId).orElseThrow(
                    ()-> new PostNotFoundException("Post not found! By Id: " + postId)
            );
            if (post != null) {
                logger.info("Success! post was founded! by id: {}",post.getId());

                user = userRepository.findById(postDto.getUserId()).orElseThrow(
                        ()-> new UserNotFoundException("User not found! By Id: "+postDto.getUserId())
                );
                if (user != null) {
                    logger.info("Success! user was founded! by id: {}",user.getId());
                    post.setUser(user);
                }

                category = categoryRepository.findById(postDto.getCategoryId()).orElseThrow(
                        ()-> new CategoryNotFoundException("Category not found! By Id: "+postDto.getCategoryId())
                );
                if (category != null){
                    logger.info("Success! category was founded! by id: {}",category.getId());
                    post.setCategory(category);
                }

                post.setTitle(postDto.getTitle());
                post.setDescription(postDto.getDescription());

                post.setCreateAt(post.getCreateAt());
                post.setUpdateAt(LocalDateTime.now().withNano(0));

                // Image update logic
                if (postImages != null && !postImages.isEmpty()) {
                    logger.info("Successfully processed posts images: {}",postImages.size());
                    List<String> postImagesPath = post.getPostImagesPath();
                    for (String filePath : postImagesPath) {
                        deleteImage = awsS3Service.deleteImage(filePath);
                    }
                    if (deleteImage) {
                        imagePaths = new ArrayList<>();
                        for (MultipartFile image : postImages) {
                            if (!image.isEmpty()) {
                                String originalFilePath = image.getOriginalFilename();
                                ObjectMetadata metadata = new ObjectMetadata();
                                metadata.setContentLength(image.getSize());

                                try {
                                    if(originalFilePath != null){
                                        String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));
                                        amazonS3.putObject(bucketName, fileName, image.getInputStream(), metadata);
                                        URL url = amazonS3.getUrl(bucketName, fileName);
                                        imagePaths.add(url.toString());
                                    }
                                } catch (Exception e) {
                                    logger.error("Error while uploading image to S3: {}", e.getMessage());
                                }
                            }
                        }
                        if (!imagePaths.isEmpty()) {
                            post.setPostImagesPath(imagePaths);
                            logger.info("Image uploaded in cloud!");
                        }
                    }
                }

                saved = postRepository.save(post);
                logger.info("Post updated successfully with ID: {}", postId);
                return mapToDto(saved);
            } else {
                logger.warn("Post with ID {} not found", postId);
            }
        } catch (Exception e) {
            logger.error("Error updating post with ID {}: {}", postId, e.getMessage());
        } finally {
            post = null;
            category = null;
            imagePaths = null;
            deleteImage = false;
            saved = null;
            logger.info("Resources cleaned up in finally block for update post.");
        }
        return null;
    }


    @Override
    public PostDetailsDto findByPostId(Long postId) {
        Post post = null;
        try {
            logger.info("Starting find post by Id functionality: {}", postId);
            post = postRepository.findById(postId).orElseThrow(
                    ()-> new PostNotFoundException("Post not found! By Id: "+postId)
            );
            if (post != null) {
                logger.info("Post details was founded! by id: {}",post.getId());
                return mapToDto(post);
            }
            logger.error("post not found by id: {} : post not exists!",postId);
            logger.warn("post not found!");
        } catch (Exception e) {
            logger.error("Error occurred while fetching post: {}", e.getMessage(), e);
        } finally {
            post = null;
            logger.info("find post by Id functionality completed");
        }
        return null;
    }

    @Override
    public List<PostDetailsDto> listOfPosts() {
        List<Post> postList = null;
        try {
            logger.info("Starting listOfPosts method");
            postList = postRepository.findAll();
            if(!postList.isEmpty()){
                return postList.stream().map(this::mapToDto).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error occurred while listing posts: {}", e.getMessage(), e);
        } finally {
            postList = null;
            logger.info("listOfPosts method completed");
        }
        return null;
    }
}

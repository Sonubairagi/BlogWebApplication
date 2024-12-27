package com.blogapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.blogapp.entity.Category;
import com.blogapp.entity.Post;
import com.blogapp.entity.User;
import com.blogapp.payload.PostDetailsDto;
import com.blogapp.payload.PostDto;
import com.blogapp.repository.CategoryRepository;
import com.blogapp.repository.PostRepository;
import com.blogapp.repository.UserRepository;
import com.blogapp.util.EmailService;
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

    @Override
    public PostDetailsDto addPost(PostDto postDto, List<MultipartFile> postImages) {
        Post post = null;
        Optional<User> opUser = null;
        Optional<Category> opCategory = null;
        List<String> imagePaths = null;

        try {
            logger.info("Starting addPost method");

            post = mapToEntity(postDto);
            opUser = userRepository.findById(postDto.getUserId());
            if (opUser.isPresent()) {
                post.setUser(opUser.get());
            }

            opCategory = categoryRepository.findById(postDto.getCategoryId());
            if (opCategory.isPresent()) {
                post.setCategory(opCategory.get());
            }

            imagePaths = new ArrayList<>();
            for (MultipartFile image : postImages) {
                if (!image.isEmpty()) {
                    String originalFilePath = image.getOriginalFilename();
                    String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(image.getSize());

                    amazonS3.putObject(bucketName, fileName, image.getInputStream(), metadata);
                    URL url = amazonS3.getUrl(bucketName, fileName);
                    imagePaths.add(url.toString());
                }
            }

            post.setPostImagesPath(imagePaths);
            post.setCreateAt(LocalDateTime.now().withNano(0));
            post.setUpdateAt(LocalDateTime.now().withNano(0));

            Post saved = postRepository.save(post);
            logger.info("Post saved successfully with ID: {}", saved.getId());

            if (saved != null) {
                String message = String.format(
                        "Hello %s,\n\n" +
                                "Congratulations! Your post titled \"%s\" has been successfully created.\n\n" +
                                "Post created at: %s\n\n" +
                                "Best Regards,\n" +
                                "[Blog App] Team",
                        saved.getUser().getUserName(),
                        saved.getTitle(),
                        saved.getCreateAt()
                );

                emailService.sendMail(
                        saved.getUser().getEmail(),
                        "Post Created Successfully",
                        message
                );
            }

            return mapToDto(saved);

        } catch (Exception e) {
            logger.error("Error occurred while adding post: {}", e.getMessage(), e);
        } finally {
            post = null;
            opUser = null;
            opCategory = null;
            imagePaths = null;
            logger.info("addPost method completed");
        }
        return null;
    }

    @Override
    public String deletePostDetails(Long postId) {
        Optional<Post> opPost = null;
        List<String> postImagesPath = null;
        boolean deleteImage = false;

        try {
            logger.info("Starting deletePostDetails method for postId: {}", postId);
            opPost = postRepository.findById(postId);
            if (opPost.isPresent()) {
                Post post = opPost.get();
                postImagesPath = post.getPostImagesPath();

                for (String filePath : postImagesPath) {
                    deleteImage = awsS3Service.deleteImage(filePath);
                }

                if (deleteImage) {
                    postRepository.deleteById(postId);
                    logger.info("Post deleted successfully with ID: {}", postId);
                    return "Post is deleted by post id : " + postId;
                }
            }

        } catch (Exception e) {
            logger.error("Error occurred while deleting post: {}", e.getMessage(), e);
        } finally {
            opPost = null;
            postImagesPath = null;
            logger.info("deletePostDetails method completed");
        }
        return "Post is not found!";
    }

    @Override
    public PostDetailsDto updatePost(Long postId, PostDto postDto, List<MultipartFile> postImages) {
        Optional<Post> opPost = null;
        Post post = null;
        Optional<User> opUser = null;
        Optional<Category> opCategory = null;
        List<String> imagePaths = null;
        boolean deleteImage = false;
        Post saved = null;

        try {
            logger.info("Starting updatePost method for postId: {}", postId);
            opPost = postRepository.findById(postId);
            if (opPost.isPresent()) {
                post = opPost.get();
                opUser = userRepository.findById(postDto.getUserId());
                if (opUser.isPresent()) {
                    logger.info("User is present!");
                    post.setUser(opUser.get());
                }
                opCategory = categoryRepository.findById(postDto.getCategoryId());
                if (opCategory.isPresent()) {
                    logger.info("Category is present!");
                    post.setCategory(opCategory.get());
                }

                post.setTitle(postDto.getTitle());
                post.setDescription(postDto.getDescription());

                post.setCreateAt(post.getCreateAt());
                post.setUpdateAt(LocalDateTime.now().withNano(0));

                // Image update logic
                if (postImages != null && !postImages.isEmpty()) {
                    List<String> postImagesPath = post.getPostImagesPath();
                    for (String filePath : postImagesPath) {
                        deleteImage = awsS3Service.deleteImage(filePath);
                    }
                    if (deleteImage) {
                        imagePaths = new ArrayList<>();
                        for (MultipartFile image : postImages) {
                            if (!image.isEmpty()) {
                                String originalFilePath = image.getOriginalFilename();
                                String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));

                                ObjectMetadata metadata = new ObjectMetadata();
                                metadata.setContentLength(image.getSize());

                                try {
                                    amazonS3.putObject(bucketName, fileName, image.getInputStream(), metadata);
                                    URL url = amazonS3.getUrl(bucketName, fileName);
                                    logger.info("Image uploaded in cloud!");
                                    imagePaths.add(url.toString());
                                } catch (Exception e) {
                                    logger.error("Error while uploading image to S3: {}", e.getMessage(), e);
                                }
                            }
                        }
                        if (imagePaths != null) {
                            post.setPostImagesPath(imagePaths);
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
            logger.error("Error updating post with ID {}: {}", postId, e.getMessage(), e);
        } finally {
            opPost = null;
            post = null;
            opUser = null;
            opCategory = null;
            imagePaths = null;
            deleteImage = false;
            saved = null;
            logger.info("Resources cleaned up in finally block for updatePost method.");
        }
        return null;
    }


    @Override
    public PostDetailsDto findByPostId(Long postId) {
        Optional<Post> opPost = null;

        try {
            logger.info("Starting findByPostId method for postId: {}", postId);
            opPost = postRepository.findById(postId);

            if (opPost.isPresent()) {
                return mapToDto(opPost.get());
            }

        } catch (Exception e) {
            logger.error("Error occurred while fetching post: {}", e.getMessage(), e);
        } finally {
            opPost = null;
            logger.info("findByPostId method completed");
        }
        return null;
    }

    @Override
    public List<PostDetailsDto> listOfPosts() {
        List<Post> postList = null;

        try {
            logger.info("Starting listOfPosts method");
            postList = postRepository.findAll();
            return postList.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error occurred while listing posts: {}", e.getMessage(), e);
        } finally {
            postList = null;
            logger.info("listOfPosts method completed");
        }
        return Collections.emptyList();
    }
}

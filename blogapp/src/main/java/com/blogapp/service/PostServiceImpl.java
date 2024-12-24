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

    public Post mapToEntity(PostDto postDto){
        return modelMapper.map(postDto,Post.class);
    }

    @Override
    public PostDetailsDto addPost(PostDto postDto,List<MultipartFile> postImages) {
        Post post = mapToEntity(postDto);

        Optional<User> opUser = userRepository.findById(postDto.getUserId());
        if(opUser.isPresent()){
            post.setUser(opUser.get());
        }
        Optional<Category> opCategory = categoryRepository.findById(postDto.getCategoryId());
        if(opCategory.isPresent()){
            post.setCategory(opCategory.get());
        }

//        List<String> imagePaths  = new ArrayList<>();
            //post images save in db
//            for(MultipartFile image:postImages){
//                if (!image.isEmpty()) {
//                    String originalFilePath = System.currentTimeMillis() + "" + image.getOriginalFilename();
//                    imagePaths.add(originalFilePath);
//                    Path fileNameAndPath = Paths.get(uploadDir + "post_images", originalFilePath);
//                    try{
//                        if(fileNameAndPath != null){Files.write(fileNameAndPath,image.getBytes());}
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        if(imagePaths != null){
//            post.setPostImagesPath(imagePaths);
//        }


        List<String> imagePaths  = new ArrayList<>();
        for(MultipartFile image:postImages){
                if (!image.isEmpty()) {
                    String originalFilePath = image.getOriginalFilename();
                    String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(image.getSize());

                    try{
                        amazonS3.putObject(bucketName,fileName,image.getInputStream(),metadata);
                        URL url = amazonS3.getUrl(bucketName,fileName);
                        imagePaths.add(url.toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        }
        if(imagePaths != null){
            post.setPostImagesPath(imagePaths);
        }

        post.setCreateAt(LocalDateTime.now().withNano(0));
        post.setUpdateAt(LocalDateTime.now().withNano(0));

        Post saved = postRepository.save(post);
        if(saved!=null){
            LocalDateTime createAt = post.getCreateAt();
            LocalDate date = createAt.toLocalDate();

            String userName = post.getUser().getUserName();
            String postTitle = post.getTitle();
            LocalDateTime createTime = post.getCreateAt();

            String message = String.format(
                    "Hello %s,\n\n" +
                            "Congratulations! Your post titled \"%s\" has been successfully created.\n\n" +
                            "Thank you for contributing to our platform. We're excited to see your content making an impact!\n\n" +
                            "Post is create by this time : %s\n\n" +
                            "Best Regards,\n" +
                            "[Application : Blog App] Team",
                    userName, postTitle, createTime
            );
            emailService.sendMail(
                    post.getUser().getEmail(),
                    "Post is create by " + post.getUser().getUserName() + " on this time : "+ date,
                    message
            );
        }
        return mapToDto(saved);
    }

    @Override
    public String deletePostDetails(Long postId) {
        Optional<Post> opPost = postRepository.findById(postId);
        if(opPost.isPresent()){
            Post post = opPost.get();
            List<String> postImagesPath = post.getPostImagesPath();
            boolean deleteImage = false;
            for(String filePath:postImagesPath){
                deleteImage = awsS3Service.deleteImage(filePath);
            }
            if(deleteImage){
                postRepository.deleteById(postId);
                return "Post is deleted by post id : "+postId;
            }
        }
        return "post is not found!";
    }

    @Override
    public PostDetailsDto updatePost(Long postId, PostDto postDto,List<MultipartFile> postImages) {
        Optional<Post> opPost = postRepository.findById(postId);
        if(opPost.isPresent()){
            Post post = opPost.get();
            Optional<User> opUser = userRepository.findById(postDto.getUserId());
            if(opUser.isPresent()){
                post.setUser(opUser.get());
            }
            Optional<Category> opCategory = categoryRepository.findById(postDto.getCategoryId());
            if(opCategory.isPresent()){
                post.setCategory(opCategory.get());
            }

            post.setTitle(postDto.getTitle());
            post.setDescription(postDto.getDescription());

            post.setCreateAt(post.getCreateAt());
            post.setUpdateAt(LocalDateTime.now().withNano(0));

            //image update
            if (postImages != null && !postImages.isEmpty()) {
                List<String> postImagesPath = post.getPostImagesPath();
                boolean deleteImage = false;
                for(String filePath:postImagesPath){
                    deleteImage = awsS3Service.deleteImage(filePath);
                }
                if(deleteImage){
                    List<String> imagePaths  = new ArrayList<>();
                    for(MultipartFile image:postImages){
                        if (!image.isEmpty()) {
                            String originalFilePath = image.getOriginalFilename();
                            String fileName = UUID.randomUUID().toString() + originalFilePath.substring(originalFilePath.lastIndexOf("."));

                            ObjectMetadata metadata = new ObjectMetadata();
                            metadata.setContentLength(image.getSize());

                            try{
                                amazonS3.putObject(bucketName,fileName,image.getInputStream(),metadata);
                                URL url = amazonS3.getUrl(bucketName,fileName);
                                imagePaths.add(url.toString());
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    if(imagePaths != null){
                        post.setPostImagesPath(imagePaths);
                    }
                }
            }

            Post saved = postRepository.save(post);
            return mapToDto(saved);
        }
        return null;
    }

    @Override
    public PostDetailsDto findByPostId(Long postId) {
        Optional<Post> opPost = postRepository.findById(postId);
        if(opPost.isPresent()){
            return mapToDto(opPost.get());
        }
        return null;
    }

    @Override
    public List<PostDetailsDto> listOfPosts() {
        List<Post> postList = postRepository.findAll();
        return postList.stream().map(post->mapToDto(post)).collect(Collectors.toList());
    }
}

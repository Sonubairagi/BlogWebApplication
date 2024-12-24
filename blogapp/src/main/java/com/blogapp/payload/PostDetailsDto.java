package com.blogapp.payload;

import com.blogapp.entity.Category;
import com.blogapp.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public class PostDetailsDto {

    private String title;
    private String description;
    private LocalDateTime updateAt;
    private List<String> postImagesPath;
    private User user;
    private Category category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public User getUser() {
        return user;
    }

    public List<String> getPostImagesPath() {
        return postImagesPath;
    }

    public void setPostImagesPath(List<String> postImagesPath) {
        this.postImagesPath = postImagesPath;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}

package com.blogapp.payload;

import java.util.List;

public class ValidationError {
    private String message;
    private List<String> details;
    private String request;

    public ValidationError(String message, List<String> details,String request) {
        this.message = message;
        this.details = details;
        this.request = request;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}

package com.blogapp.payload;
import java.util.Date;

public class ErrorDetails {
    private Date dateTime;
    private String message;
    private String request;

    public ErrorDetails(Date dateTime, String message, String request) {
        this.dateTime = dateTime;
        this.message = message;
        this.request = request;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}

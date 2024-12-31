package com.xplore.payload;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    private String timestamp;
    private String correlationId;
    private String apiName;
    private String errorMessage;
    private int statusCode;
    private String execution; // Success/Failed

    public ApiErrorResponse(String correlationId, String apiName, String errorMessage, int statusCode, String execution) {
        this.timestamp = LocalDateTime.now().toString();
        this.correlationId = correlationId;
        this.apiName = apiName;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.execution = execution;
    }

    @Override
    public String toString() {
        return "ApiErrorResponse{" +
                "timestamp='" + timestamp + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", apiName='" + apiName + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", statusCode=" + statusCode +
                ", execution='" + execution + '\'' +
                '}';
    }

    // Getters and Setters
    public String getTimestamp() {
        return timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getApiName() {
        return apiName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getExecution() {
        return execution;
    }
}

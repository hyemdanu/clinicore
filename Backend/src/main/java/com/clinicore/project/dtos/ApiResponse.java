package com.clinicore.project.dtos;

/**
 * Standardized API response format for all endpoints
 * @param <T> Type of the data payload
 */
public class ApiResponse<T> {
    private int statusCode;
    private T data;
    private String error;
    
    // Success constructor
    public ApiResponse(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
        this.error = null;
    }
    
    // Error constructor
    public ApiResponse(int statusCode, String error) {
        this.statusCode = statusCode;
        this.data = null;
        this.error = error;
    }
    
    // Default constructor
    public ApiResponse() {
    }
    
    // Getters and setters
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    // Utility methods
    public boolean isSuccess() {
        return statusCode == 1;
    }
    
    public boolean hasError() {
        return statusCode == -1;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "statusCode=" + statusCode +
                ", data=" + data +
                ", error='" + error + '\'' +
                '}';
    }
}
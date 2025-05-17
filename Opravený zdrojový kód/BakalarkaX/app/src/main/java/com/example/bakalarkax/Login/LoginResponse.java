package com.example.bakalarkax.Login;

public class LoginResponse {
    private boolean success;
    private String message;
    private String userId;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUserId() {
        return userId;
    }
}
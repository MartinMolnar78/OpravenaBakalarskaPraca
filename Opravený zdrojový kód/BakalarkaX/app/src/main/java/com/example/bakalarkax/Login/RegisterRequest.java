package com.example.bakalarkax.Login;

public class RegisterRequest {
    private String first_name;
    private String last_name;
    private String email;
    private String password;

    public RegisterRequest(String first_name, String last_name, String email, String password) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.password = password;
    }
}

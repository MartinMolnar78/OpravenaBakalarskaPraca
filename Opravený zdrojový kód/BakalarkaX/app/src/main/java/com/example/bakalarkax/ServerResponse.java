package com.example.bakalarkax;

import com.example.bakalarkax.Clothes.ClothingItem;

public class ServerResponse {
    private boolean success;
    private String message;
    private ClothingItem data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ClothingItem getData() {
        return data;
    }
}

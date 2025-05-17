package com.example.bakalarkax.OutfitX;

import java.util.List;

public class OutfitRequest {
    private int id_user;
    private String outfit_name;
    private List<Integer> clothing_items;

    public OutfitRequest(int id_user, String outfit_name, List<Integer> clothing_items) {
        this.id_user = id_user;
        this.outfit_name = outfit_name;
        this.clothing_items = clothing_items;
    }
}

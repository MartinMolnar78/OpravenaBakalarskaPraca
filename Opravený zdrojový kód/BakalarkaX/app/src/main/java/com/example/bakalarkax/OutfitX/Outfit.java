package com.example.bakalarkax.OutfitX;

import java.util.List;

public class Outfit {
    private int idOutfit;
    private String outfitName;
    private String createdAt;
    private List<ClothingItem> clothingItems;

    public int getIdOutfit() {
        return idOutfit;
    }

    public String getOutfitName() {
        return outfitName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public List<ClothingItem> getClothingItems() {
        return clothingItems;
    }

    public static class ClothingItem {
        private String type;
        private String photo;

        public String getType() {
            return type;
        }

        public String getImageUrl() {
            if (photo != null && !photo.isEmpty()) {
                return "http://martan78.euweb.cz/uploads/" + photo;
            } else {
                return "http://martan78.euweb.cz/uploads/default_image.png";
            }
        }
    }
}

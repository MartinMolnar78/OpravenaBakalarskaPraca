package com.example.bakalarkax.Clothes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ClothingItem {
    @SerializedName("id")
    private int id_clothing;

    @SerializedName("photo")
    private String photo;

    @SerializedName("brand")
    private String brand;

    @SerializedName("category")
    private String category;

    @SerializedName("season")
    private String season;

    @SerializedName("id_qr")
    private int id_qr;

    @SerializedName("type")
    private String type;

    @SerializedName("success")
    private boolean success;

    @SerializedName("dates")
    private List<String> dates;

    public String getImageUrl() {
        if (photo != null && !photo.isEmpty()) {
            return "http://martan78.euweb.cz/uploads/" + photo;
        } else {
            return "http://martan78.euweb.cz/uploads/default_image.png";
        }
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public int getIdQr() {
        return id_qr;
    }

    public void setIdQr(int idQr) {
        this.id_qr = idQr;
    }

    public int getIdClothing() {
        return id_clothing;
    }

    public String getType() {
        return type;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getDates() {
        return dates;
    }


}

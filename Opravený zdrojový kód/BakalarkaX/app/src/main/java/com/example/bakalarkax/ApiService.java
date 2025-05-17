package com.example.bakalarkax;

import com.example.bakalarkax.Clothes.ClothingItem;
import com.example.bakalarkax.Login.LoginRequest;
import com.example.bakalarkax.Login.LoginResponse;
import com.example.bakalarkax.Login.RegisterRequest;
import com.example.bakalarkax.OutfitX.OutfitRequest;
import com.example.bakalarkax.OutfitX.OutfitResponse;


import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login.php")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("register.php")
    Call<LoginResponse> registerUser(@Body RegisterRequest registerRequest);

    @Multipart
    @POST("upload.php") // Názov PHP skriptu
    Call<ServerResponse> addClothing(
            @Part("id_user") RequestBody idUser,
            @Part("id_qr") RequestBody idQr,
            @Part("type") RequestBody type,
            @Part("brand") RequestBody brand,
            @Part("category") RequestBody category,
            @Part("season") RequestBody season,
            @Part("color") RequestBody color,
            @Part MultipartBody.Part photo
    );

    @GET("checkQrCode.php")
    Call<Boolean> isQrCodeAssigned(@Query("user_id") String userId, @Query("qr_code") String qrCode);

    @GET("get_clothes.php")
    Call<List<ClothingItem>> getClothes(@Query("user_id") String userId);

    @GET("get_clothing_by_qr.php")
    Call<ClothingItem> getClothingByQr(@Query("user_id") String userId, @Query("number") String qrNumber);

    @GET("get_clothing_usage.php")
    Call<ClothingItem> getClothingUsage(@Query("id_clothing") int idClothing);

    @GET("get_clothes_by_category.php")
    Call<List<ClothingItem>> getClothesByCategory(@Query("user_id") String userId, @Query("type") String category);

    @POST("saveOutfit.php")
    Call<ResponseBody> saveOutfit(@Body OutfitRequest outfitRequest);

    @GET("getOutfits.php")
    Call<OutfitResponse> getOutfitsFiltered(
            @Query("id_user") int userId,
            @Query("year") int year,
            @Query("month") int month
    );


    @POST("updateQrCode.php")
    Call<Void> updateClothingQrCode(@Query("clothing_id") int clothingId, @Query("qr_code") int qrCode);

    @GET("deleteClothing.php")
    Call<Void> deleteClothing(@Query("clothing_id") int clothingId);

    @GET("get_top_clothing.php")
    Call<List<ClothingItem>> getTopUsedClothing(@Query("user_id") int userId);

    @GET("get_outfits_by_weekday.php")
    Call<OutfitResponse> getOutfitsByDay(
            @Query("id_user") int userId,
            @Query("day") String dayName
    );
}
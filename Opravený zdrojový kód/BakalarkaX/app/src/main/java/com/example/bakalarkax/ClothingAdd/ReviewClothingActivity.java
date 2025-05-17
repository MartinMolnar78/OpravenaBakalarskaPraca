package com.example.bakalarkax.ClothingAdd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bakalarkax.DrawerManager;
import com.example.bakalarkax.ApiService;
import com.example.bakalarkax.RetrofitClient;
import com.example.bakalarkax.ServerResponse;
import com.example.bakalarkax.R;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewClothingActivity extends AppCompatActivity {
    private DrawerManager drawerManager;
    private ImageView imageViewClothing;
    private Button  buttonAdd;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_clothing);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerManager = new DrawerManager(this, drawerLayout, navigationView, toolbar);

        imageViewClothing = findViewById(R.id.imageViewClothing);
        buttonAdd = findViewById(R.id.buttonAdd);


        Intent intent = getIntent();


        String photoFilePath = intent.getStringExtra("photoFilePath");
        String clothingBrand = intent.getStringExtra("clothingBrand");
        String type = intent.getStringExtra("type");
        String category = intent.getStringExtra("category");
        String color = intent.getStringExtra("color");
        String season = intent.getStringExtra("season");
        String idQr = intent.getStringExtra("idQr");
        if (photoFilePath != null) {
            photoFile = new File(photoFilePath);
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageViewClothing.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "No photo available.", Toast.LENGTH_SHORT).show();
        }

        TextView textViewBrand = findViewById(R.id.textViewBrand);
        TextView textViewType = findViewById(R.id.textViewType);
        TextView textViewCategory = findViewById(R.id.textViewCategory);
        TextView textViewColor = findViewById(R.id.textViewColor);
        TextView textViewSeason = findViewById(R.id.textViewSeason);
        TextView textViewQr = findViewById(R.id.textViewQr);

        textViewBrand.setText("Brand: " + (clothingBrand != null ? clothingBrand : "N/A"));
        textViewType.setText("Type: " + (type != null ? type : "N/A"));
        textViewCategory.setText("Category: " + (category != null ? category : "N/A"));
        textViewColor.setText("Color: " + (color != null ? color : "N/A"));
        textViewSeason.setText("Season: " + (season != null ? season : "N/A"));
        textViewQr.setText("QR Code: " + ((idQr != null && !idQr.equals("0")) ? idQr : "Without QR"));

        buttonAdd.setOnClickListener(v -> uploadClothingData());
    }

    private void uploadClothingData() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Intent intent = getIntent();
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");
        String type = intent.getStringExtra("type");
        String brand = intent.getStringExtra("clothingBrand");
        String category = intent.getStringExtra("category");
        String season = intent.getStringExtra("season");
        String color = intent.getStringExtra("color");
        String idQr = intent.getStringExtra("idQr");

        RequestBody idUserPart = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody idQrPart = RequestBody.create(MediaType.parse("text/plain"), idQr != null ? idQr : "");
        RequestBody typePart = RequestBody.create(MediaType.parse("text/plain"), type != null ? type : "");
        RequestBody brandPart = RequestBody.create(MediaType.parse("text/plain"), brand != null ? brand : "");
        RequestBody categoryPart = RequestBody.create(MediaType.parse("text/plain"), category != null ? category : "");
        RequestBody seasonPart = RequestBody.create(MediaType.parse("text/plain"), season != null ? season : "");
        RequestBody colorPart = RequestBody.create(MediaType.parse("text/plain"), color != null ? color : "");

        if (photoFile == null) {
            Toast.makeText(this, "Photo file is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody photoRequestBody = RequestBody.create(MediaType.parse("image/*"), photoFile);
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo", photoFile.getName(), photoRequestBody);

        Call<ServerResponse> call = apiService.addClothing(
                idUserPart, idQrPart, typePart, brandPart, categoryPart, seasonPart, colorPart, photoPart
        );

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ReviewClothingActivity.this, "Clothing added successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ReviewClothingActivity.this, ClothingAddActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ReviewClothingActivity.this, "Failed to add clothing.", Toast.LENGTH_SHORT).show();
                    Log.e("UploadError", "Response code: " + response.code());
                    if (response.errorBody() != null) {
                        Log.e("UploadError", "Response body: " + response.errorBody().toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Toast.makeText(ReviewClothingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

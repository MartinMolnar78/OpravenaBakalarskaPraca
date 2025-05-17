package com.example.bakalarkax;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bakalarkax.Clothes.ClothingItem;
import com.example.bakalarkax.OutfitX.Outfit;
import com.example.bakalarkax.OutfitX.OutfitResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;


public class DashboardActivity extends AppCompatActivity {

    private DrawerManager drawerManager;
    private LinearLayout topUsedContainer;
    private TextView textWelcome;
    private int userId;
    private Spinner spinnerDay;
    private LinearLayout lastOutfitsContainer;

    private final String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static final int REQUEST_ALL_PERMISSIONS = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        EdgeToEdge.enable(this);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerManager = new DrawerManager(this, drawerLayout, navigationView, toolbar);

        topUsedContainer = findViewById(R.id.topUsedContainer);
        textWelcome = findViewById(R.id.textWelcome);
        spinnerDay = findViewById(R.id.spinnerDay);
        lastOutfitsContainer = findViewById(R.id.lastOutfitsContainer);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userIdStr = prefs.getString("user_id", "-1");
        userId = Integer.parseInt(userIdStr);
        String userName = prefs.getString("user_name", "User");


        if (userId != -1) {
            loadTopUsedClothing(userId);
        } else {
            Toast.makeText(this, "User ID missing", Toast.LENGTH_SHORT).show();
        }

        Button scanQrButton = findViewById(R.id.scanQrButton);
        scanQrButton.setOnClickListener(v -> startQrScanner());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysOfWeek);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(adapter);

        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDay = daysOfWeek[position];
                loadLastOutfitsByDay(userId, selectedDay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        requestAllPermissions();

    }

    private void startQrScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrData = result.getContents();
            String qrNumber = parseQrNumber(qrData);
            if (qrNumber != null) {
                fetchClothingByQr(qrNumber);
            } else {
                Toast.makeText(this, "Invalid QR code format!", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String parseQrNumber(String qrData) {
        for (String field : qrData.split(",")) {
            String[] kv = field.split(":");
            if (kv.length == 2 && kv[0].trim().equalsIgnoreCase("number"))
                return kv[1].trim();
        }
        return null;
    }

    private void fetchClothingByQr(String qrNumber) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getClothingByQr(String.valueOf(userId), qrNumber).enqueue(new Callback<ClothingItem>() {
            @Override
            public void onResponse(Call<ClothingItem> call, Response<ClothingItem> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ClothingItem item = response.body();
                    ClothingDetailsDialog.show(DashboardActivity.this, item, false, null);
                } else {
                    Toast.makeText(DashboardActivity.this, "No item found for this QR", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ClothingItem> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Failed to fetch item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTopUsedClothing(int userId) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<ClothingItem>> call = apiService.getTopUsedClothing(userId);

        call.enqueue(new Callback<List<ClothingItem>>() {
            @Override
            public void onResponse(Call<List<ClothingItem>> call, Response<List<ClothingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    topUsedContainer.removeAllViews();
                    for (ClothingItem item : response.body()) {
                        ImageView imageView = new ImageView(DashboardActivity.this);
                        int size = getResources().getDisplayMetrics().widthPixels / 5;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, (size * 4) / 3);
                        params.setMargins(8, 8, 8, 8);
                        imageView.setLayoutParams(params);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        Picasso.get().load(item.getImageUrl()).into(imageView);
                        topUsedContainer.addView(imageView);
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "No items found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClothingItem>> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error loading top used items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLastOutfitsByDay(int userId, String dayName) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<OutfitResponse> call = apiService.getOutfitsByDay(userId, dayName);

        call.enqueue(new Callback<OutfitResponse>() {
            @Override
            public void onResponse(Call<OutfitResponse> call, Response<OutfitResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Outfit> outfits = response.body().outfits;
                    lastOutfitsContainer.removeAllViews();

                    for (Outfit outfit : outfits) {
                        View itemView = getLayoutInflater().inflate(R.layout.item_outfit, null);

                        TextView name = itemView.findViewById(R.id.textViewOutfitName);
                        TextView date = itemView.findViewById(R.id.textViewOutfitDate);

                        name.setText(outfit.getOutfitName());
                        date.setText(formatDateTime(outfit.getCreatedAt()));

                        LinearLayout head = itemView.findViewById(R.id.headContainer);
                        LinearLayout upper = itemView.findViewById(R.id.upperContainer);
                        LinearLayout lower = itemView.findViewById(R.id.lowerContainer);
                        LinearLayout shoes = itemView.findViewById(R.id.shoesContainer);

                        for (Outfit.ClothingItem item : outfit.getClothingItems()) {
                            ImageView img = new ImageView(DashboardActivity.this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
                            params.setMargins(8, 0, 8, 0);
                            img.setLayoutParams(params);
                            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Picasso.get().load(item.getImageUrl()).into(img);

                            switch (item.getType().toLowerCase()) {
                                case "head": head.addView(img); break;
                                case "upper": upper.addView(img); break;
                                case "lower": lower.addView(img); break;
                                case "shoes": shoes.addView(img); break;
                            }
                        }

                        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        itemParams.setMargins(16, 0, 16, 0);
                        itemView.setLayoutParams(itemParams);

                        lastOutfitsContainer.addView(itemView);
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "No outfits found for this day", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OutfitResponse> call, Throwable t) {
                Toast.makeText(DashboardActivity.this, "Error loading outfits", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDateTime(String inputDateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(inputDateTime);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d.M.yyyy HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            return inputDateTime;
        }
    }

    private void requestAllPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_ALL_PERMISSIONS);
        }
    }
}

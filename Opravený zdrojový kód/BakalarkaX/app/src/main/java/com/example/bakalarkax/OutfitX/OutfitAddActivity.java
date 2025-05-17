package com.example.bakalarkax.OutfitX;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakalarkax.Clothes.ClothingAdapter2;
import com.example.bakalarkax.Clothes.ClothingItem;
import com.example.bakalarkax.ClothingDetailsDialog;
import com.example.bakalarkax.DrawerManager;
import com.example.bakalarkax.ApiService;
import com.example.bakalarkax.RetrofitClient;
import com.example.bakalarkax.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutfitAddActivity extends AppCompatActivity {
    private DrawerManager drawerManager;
    private LinearLayout headContainer, upperContainer, lowerContainer, shoesContainer;
    private Button saveOutfitButton;
    private EditText outfitNameEditText;
    private SharedPreferences sharedPreferences;
    private ImageView headIcon, upperIcon, lowerIcon, shoesIcon;


    private final int MAX_ITEMS = 5;

    private final List<Integer> selectedHeadItems = new ArrayList<>();
    private final List<Integer> selectedUpperItems = new ArrayList<>();
    private final List<Integer> selectedLowerItems = new ArrayList<>();
    private final List<Integer> selectedShoesItems = new ArrayList<>();
    private String selectedCategory;
    private boolean isManual = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_outfit_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        drawerManager = new DrawerManager(this, drawerLayout, navigationView, toolbar);

        headContainer = findViewById(R.id.headContainer);
        upperContainer = findViewById(R.id.upperContainer);
        lowerContainer = findViewById(R.id.lowerContainer);
        shoesContainer = findViewById(R.id.shoesContainer);
        saveOutfitButton = findViewById(R.id.saveOutfitButton);

        headContainer.setOnClickListener(v -> showSelectionDialog("head"));
        upperContainer.setOnClickListener(v -> showSelectionDialog("upper"));
        lowerContainer.setOnClickListener(v -> showSelectionDialog("lower"));
        shoesContainer.setOnClickListener(v -> showSelectionDialog("shoes"));

        outfitNameEditText = findViewById(R.id.outfitNameEditText);

        headIcon = findViewById(R.id.headIcon);
        upperIcon = findViewById(R.id.upperIcon);
        lowerIcon = findViewById(R.id.lowerIcon);
        shoesIcon = findViewById(R.id.shoesIcon);




        outfitNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                saveOutfitName(v.getText().toString());
                return true;
            }
            return false;
        });

        saveOutfitButton.setOnClickListener(v -> saveOutfit());
    }

    private void showSelectionDialog(String category) {
        selectedCategory = category;

        new AlertDialog.Builder(this)
                .setTitle("Choose an option")
                .setItems(new CharSequence[]{"Scan QR Code", "Select Manually"}, (dialog, which) -> {
                    if (which == 0) {
                        isManual = false;
                        scanQrCode();
                    } else {
                        isManual = true;
                        showClothingSelectionDialog(category);
                    }
                }).show();
    }

    private void scanQrCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    private String parseQrNumber(String qrData) {
        String[] fields = qrData.split(",");
        for (String field : fields) {
            String[] keyValue = field.split(":");
            if (keyValue.length == 2 && keyValue[0].trim().equalsIgnoreCase("number")) {
                return keyValue[1].trim();
            }
        }
        return null;
    }

    private void fetchClothingData(String qrData) {
        String qrNumber = parseQrNumber(qrData);
        if (qrNumber == null || Integer.parseInt(qrNumber) <= 0) {
            Toast.makeText(this, "Invalid QR code: number must be greater than 0!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getClothingByQr(userId, qrNumber).enqueue(new Callback<ClothingItem>() {
            @Override
            public void onResponse(Call<ClothingItem> call, Response<ClothingItem> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ClothingItem clothingItem = response.body();
                    if (!clothingItem.getType().equalsIgnoreCase(selectedCategory)) {
                        Toast.makeText(OutfitAddActivity.this, "Wrong category!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ClothingDetailsDialog.show(
                            OutfitAddActivity.this,
                            clothingItem,
                            true,
                            item -> {
                                addClothingItem(selectedCategory, item);
                                isManual = false;
                            }
                    );

                } else {
                    Toast.makeText(OutfitAddActivity.this, "No clothing found for this QR code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ClothingItem> call, Throwable t) {
                Toast.makeText(OutfitAddActivity.this, "Error fetching clothing data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClothingSelectionDialog(String category) {
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getClothesByCategory(userId, category).enqueue(new Callback<List<ClothingItem>>() {
            @Override
            public void onResponse(Call<List<ClothingItem>> call, Response<List<ClothingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OutfitAddActivity.this);
                    View view = getLayoutInflater().inflate(R.layout.activity_bottom_show_clothes, null);
                    bottomSheetDialog.setContentView(view);

                    RecyclerView recyclerView = view.findViewById(R.id.recyclerViewClothing);
                    recyclerView.setLayoutManager(new GridLayoutManager(OutfitAddActivity.this, 2));

                    ClothingAdapter2 adapter = new ClothingAdapter2(OutfitAddActivity.this, response.body());
                    recyclerView.setAdapter(adapter);

                    adapter.setOnItemClickListener(item -> {
                        ClothingDetailsDialog.show(
                                OutfitAddActivity.this,
                                item,
                                true,
                                selected -> {
                                    addClothingItem(selectedCategory, selected);
                                    isManual = false;
                                    bottomSheetDialog.dismiss();
                                }
                        );
                    });


                    bottomSheetDialog.show();

                } else {
                    Toast.makeText(OutfitAddActivity.this, "No clothing found for this category.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClothingItem>> call, Throwable t) {
                Toast.makeText(OutfitAddActivity.this, "Error fetching clothing: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addClothingItem(String category, ClothingItem clothingItem) {
        List<Integer> selectedItems;
        LinearLayout container;
        ImageView iconView;

        switch (category) {
            case "head":
                selectedItems = selectedHeadItems;
                container = headContainer;
                iconView = headIcon;
                break;
            case "upper":
                selectedItems = selectedUpperItems;
                container = upperContainer;
                iconView = upperIcon;
                break;
            case "lower":
                selectedItems = selectedLowerItems;
                container = lowerContainer;
                iconView = lowerIcon;
                break;
            case "shoes":
                selectedItems = selectedShoesItems;
                container = shoesContainer;
                iconView = shoesIcon;
                break;
            default:
                return;
        }

        if (selectedItems.contains(clothingItem.getIdClothing())) {
            Toast.makeText(this, "This clothing item is already added!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedItems.size() >= MAX_ITEMS) {
            Toast.makeText(this, "Max 5 items allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedItems.add(clothingItem.getIdClothing());

        if (iconView != null) iconView.setVisibility(View.GONE);

        updateContainer(container, clothingItem);
    }


    private void updateContainer(LinearLayout container, ClothingItem clothingItem) {
        ImageView imageView = new ImageView(this);

        int width = calculateResponsiveImageSize();  // napr. 180px
        int height = (width * 4) / 3;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Picasso.get().load(clothingItem.getImageUrl()).into(imageView);
        container.addView(imageView);

        imageView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(container, imageView, clothingItem);
            return true;
        });
    }


    private void showDeleteConfirmationDialog(LinearLayout container, ImageView imageView, ClothingItem clothingItem) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Clothing")
                .setMessage("Do you want to remove this item from the outfit?")
                .setPositiveButton("Remove", (dialog, which) -> removeClothingItem(container, imageView, clothingItem))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeClothingItem(LinearLayout container, ImageView imageView, ClothingItem clothingItem) {
        container.removeView(imageView);

        List<Integer> selectedItems;
        ImageView iconView;

        switch (selectedCategory) {
            case "head":
                selectedItems = selectedHeadItems;
                iconView = headIcon;
                break;
            case "upper":
                selectedItems = selectedUpperItems;
                iconView = upperIcon;
                break;
            case "lower":
                selectedItems = selectedLowerItems;
                iconView = lowerIcon;
                break;
            case "shoes":
                selectedItems = selectedShoesItems;
                iconView = shoesIcon;
                break;
            default:
                return;
        }

        selectedItems.remove((Integer) clothingItem.getIdClothing());

        if (selectedItems.isEmpty() && iconView != null) {
            iconView.setVisibility(View.VISIBLE);
        }

        Toast.makeText(this, "Clothing removed!", Toast.LENGTH_SHORT).show();
    }


    private void saveOutfitName(String name) {
        sharedPreferences.edit().putString("outfit_name", name).apply();
        Toast.makeText(this, "Outfit name saved!", Toast.LENGTH_SHORT).show();
    }

    private void saveOutfit() {
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");
        String outfitName = outfitNameEditText.getText().toString();

        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (outfitName.isEmpty()) {
            outfitName = "My Outfit";
        }


        List<Integer> clothingItems = new ArrayList<>();
        clothingItems.addAll(selectedHeadItems);
        clothingItems.addAll(selectedUpperItems);
        clothingItems.addAll(selectedLowerItems);
        clothingItems.addAll(selectedShoesItems);

        if (clothingItems.isEmpty()) {
            Toast.makeText(this, "No clothing items selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        OutfitRequest outfitRequest = new OutfitRequest(Integer.parseInt(userId), outfitName, clothingItems);
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.saveOutfit(outfitRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OutfitAddActivity.this, "Outfit saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                } else {
                    Toast.makeText(OutfitAddActivity.this, "Failed to save outfit!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(OutfitAddActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateResponsiveImageSize() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        return screenWidth / 5 - 32;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            fetchClothingData(result.getContents());
        } else {
            Toast.makeText(this, "QR Code scanning cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}

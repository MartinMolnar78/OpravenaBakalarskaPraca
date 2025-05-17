package com.example.bakalarkax.Clothes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakalarkax.ApiService;
import com.example.bakalarkax.DrawerManager;
import com.example.bakalarkax.R;
import com.example.bakalarkax.RetrofitClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyClothesActivity extends AppCompatActivity {
    private DrawerManager drawerManager;
    private RecyclerView recyclerViewClothes;
    private ClothingAdapter adapter;
    private List<ClothingItem> fullClothingList = new ArrayList<>();

    private EditText editTextSearch;
    private Spinner spinnerSeason, spinnerBodyPart, spinnerCategory;

    private ClothingItem selectedItemForQrChange;
    private String pendingQrCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_clothes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        recyclerViewClothes = findViewById(R.id.recyclerViewClothes);

        drawerManager = new DrawerManager(this, drawerLayout, findViewById(R.id.nav_view), toolbar);
        recyclerViewClothes.setLayoutManager(new GridLayoutManager(this, 3));

        editTextSearch = findViewById(R.id.editTextSearch);
        spinnerSeason = findViewById(R.id.spinnerSeasonFilter);
        spinnerBodyPart = findViewById(R.id.spinnerBodyPartFilter);
        spinnerCategory = findViewById(R.id.spinnerCategoryFilter);

        ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(this, R.array.filter_category_season, android.R.layout.simple_spinner_item);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeason.setAdapter(seasonAdapter);
        spinnerSeason.setSelection(0);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.filter_category_main, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBodyPart.setAdapter(typeAdapter);
        spinnerBodyPart.setSelection(0);

        ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        spinnerBodyPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int subArrayResId = -1;
                switch (position) {
                    case 1: subArrayResId = R.array.subcategory_head; break;
                    case 2: subArrayResId = R.array.subcategory_upper; break;
                    case 3: subArrayResId = R.array.subcategory_lower; break;
                    case 4: subArrayResId = R.array.subcategory_shoes; break;
                }
                if (subArrayResId != -1) {
                    ArrayAdapter<CharSequence> newCategoryAdapter = new ArrayAdapter<>(MyClothesActivity.this, android.R.layout.simple_spinner_item, getResources().getStringArray(subArrayResId));
                    newCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(newCategoryAdapter);
                    spinnerCategory.setSelection(0);
                } else {
                    spinnerCategory.setAdapter(null);
                }
                filterClothes();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filterClothes(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSeason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filterClothes(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterClothes(); }
        });

        loadClothesFromApi();
    }

    private void filterClothes() {
        if (adapter == null) return;

        String query = editTextSearch.getText().toString().toLowerCase();
        String season = spinnerSeason.getSelectedItem() != null ? spinnerSeason.getSelectedItem().toString() : "";
        String bodyPart = spinnerBodyPart.getSelectedItem() != null ? spinnerBodyPart.getSelectedItem().toString() : "";
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";

        List<ClothingItem> filteredList = new ArrayList<>();
        for (ClothingItem item : fullClothingList) {
            boolean matchesQuery = item.getBrand().toLowerCase().contains(query);
            boolean matchesSeason = season.equals("All") || season.isEmpty() || item.getSeason().equalsIgnoreCase(season);
            boolean matchesBodyPart = bodyPart.equals("All") || bodyPart.isEmpty() || item.getType().equalsIgnoreCase(bodyPart);
            boolean matchesCategory = category.equals("All") || category.isEmpty() || item.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesSeason && matchesBodyPart && matchesCategory) {
                filteredList.add(item);
            }
        }

        adapter.updateList(filteredList);
    }

    private void loadClothesFromApi() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");

        Call<List<ClothingItem>> call = apiService.getClothes(userId);
        call.enqueue(new Callback<List<ClothingItem>>() {
            @Override
            public void onResponse(Call<List<ClothingItem>> call, Response<List<ClothingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullClothingList = response.body();
                    adapter = new ClothingAdapter(MyClothesActivity.this, new ArrayList<>(fullClothingList));
                    recyclerViewClothes.setAdapter(adapter);

                    adapter.setOnItemClickListener(item -> {
                        com.example.bakalarkax.ClothingDetailsDialog.show(MyClothesActivity.this, item, false, null);
                    });

                    adapter.setOnItemLongClickListener(item -> showInitialQrDialog(item));
                } else {
                    Toast.makeText(MyClothesActivity.this, "Failed to load clothes.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ClothingItem>> call, Throwable t) {
                Toast.makeText(MyClothesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInitialQrDialog(ClothingItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options for " + item.getBrand());
        builder.setItems(new CharSequence[]{"Scan QR", "Delete"}, (dialog, which) -> {
            if (which == 0) {
                selectedItemForQrChange = item;
                new IntentIntegrator(this)
                        .setPrompt("Scan new QR code")
                        .setOrientationLocked(true)
                        .initiateScan();
            } else if (which == 1) {
                confirmAndDeleteItem(item);
            }
        });
        builder.show();
    }
    private void confirmAndDeleteItem(ClothingItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Do you really want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                    apiService.deleteClothing(item.getIdClothing()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MyClothesActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                loadClothesFromApi();
                            } else {
                                Toast.makeText(MyClothesActivity.this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(MyClothesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }


    private String parseQrNumber(String qrData) {
        for (String field : qrData.split(",")) {
            String[] kv = field.split(":");
            if (kv.length == 2 && kv[0].trim().equalsIgnoreCase("number")) {
                return kv[1].trim();
            }
        }
        return null;
    }

    private void checkAndConfirmQr(String qrNumber) {
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.isQrCodeAssigned(userId, qrNumber).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.FALSE.equals(response.body())) {
                    new AlertDialog.Builder(MyClothesActivity.this)
                            .setTitle("Confirmation")
                            .setMessage("Do you really want to assign QR code: " + qrNumber + "?")
                            .setPositiveButton("Yes", (dialog, which) ->
                                    updateClothingQrCode(selectedItemForQrChange.getIdClothing(), Integer.parseInt(qrNumber)))
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    Toast.makeText(MyClothesActivity.this, "QR code is already assigned!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(MyClothesActivity.this, "Error verifying QR code", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClothingQrCode(int clothingId, int newQrCode) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.updateClothingQrCode(clothingId, newQrCode).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyClothesActivity.this, "QR code was successfully updated!", Toast.LENGTH_SHORT).show();
                    loadClothesFromApi();
                } else {
                    Toast.makeText(MyClothesActivity.this, "Failed to update QR code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MyClothesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null && selectedItemForQrChange != null) {
            String qrData = result.getContents();
            String qrNumber = parseQrNumber(qrData);
            if (qrNumber != null) {
                checkAndConfirmQr(qrNumber);
            } else {
                Toast.makeText(this, "QR code is not in a valid format", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

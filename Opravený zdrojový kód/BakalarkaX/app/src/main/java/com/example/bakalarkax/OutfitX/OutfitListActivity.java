package com.example.bakalarkax.OutfitX;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakalarkax.ApiService;
import com.example.bakalarkax.DrawerManager;
import com.example.bakalarkax.R;
import com.example.bakalarkax.RetrofitClient;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutfitListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOutfits;
    private OutfitAdapter adapter;
    private List<Outfit> outfitList = new ArrayList<>();
    private DrawerManager drawerManager;
    private Spinner spinnerYear;
    private Spinner spinnerMonth;

    private static final LinkedHashMap<String, Integer> MONTH_MAP = new LinkedHashMap<>();
    static {
        MONTH_MAP.put("January", 1);
        MONTH_MAP.put("February", 2);
        MONTH_MAP.put("March", 3);
        MONTH_MAP.put("April", 4);
        MONTH_MAP.put("May", 5);
        MONTH_MAP.put("June", 6);
        MONTH_MAP.put("July", 7);
        MONTH_MAP.put("August", 8);
        MONTH_MAP.put("September", 9);
        MONTH_MAP.put("October", 10);
        MONTH_MAP.put("November", 11);
        MONTH_MAP.put("December", 12);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_outfit_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerManager = new DrawerManager(this, drawerLayout, navigationView, toolbar);


        recyclerViewOutfits = findViewById(R.id.recyclerViewOutfits);
        recyclerViewOutfits.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OutfitAdapter(this, outfitList);
        recyclerViewOutfits.setAdapter(adapter);

        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerMonth = findViewById(R.id.spinnerMonth);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;

        List<String> years = new ArrayList<>();
        years.add(String.valueOf(currentYear));

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(0);

        List<String> monthNames = new ArrayList<>(MONTH_MAP.keySet());
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthNames);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        int currentMonthIndex = new ArrayList<>(MONTH_MAP.values()).indexOf(currentMonth);
        spinnerMonth.setSelection(currentMonthIndex);

        fetchOutfits(currentYear, currentMonth);
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedMonthName = spinnerMonth.getSelectedItem().toString();
                int month = MONTH_MAP.get(selectedMonthName);
                int year = Integer.parseInt(spinnerYear.getSelectedItem().toString());
                fetchOutfits(year, month);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        };

        spinnerYear.setOnItemSelectedListener(filterListener);
        spinnerMonth.setOnItemSelectedListener(filterListener);
    }

    private void fetchOutfits(int year, int month) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userIdString = sharedPreferences.getString("user_id", "-1");

        int userId;
        try {
            userId = Integer.parseInt(userIdString);
        } catch (NumberFormatException e) {
            userId = -1;
        }

        if (userId == -1) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<OutfitResponse> call = apiService.getOutfitsFiltered(userId, year, month);

        call.enqueue(new Callback<OutfitResponse>() {
            @Override
            public void onResponse(Call<OutfitResponse> call, Response<OutfitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OutfitResponse outfitResponse = response.body();
                    if (outfitResponse.success) {
                        adapter.updateList(outfitResponse.outfits);
                    } else {
                        Toast.makeText(OutfitListActivity.this, "No outfits found", Toast.LENGTH_SHORT).show();
                        adapter.updateList(new ArrayList<>());
                    }
                } else {
                    Toast.makeText(OutfitListActivity.this, "Error fetching outfits", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OutfitResponse> call, Throwable t) {
                Toast.makeText(OutfitListActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String formatDateTime(String rawDateTime) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = originalFormat.parse(rawDateTime);

            SimpleDateFormat desiredFormat = new SimpleDateFormat("d.M.yyyy HH:mm", Locale.getDefault());
            return desiredFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return rawDateTime;
        }
    }

}

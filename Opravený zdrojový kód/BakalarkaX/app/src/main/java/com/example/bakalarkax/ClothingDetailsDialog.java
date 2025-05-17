package com.example.bakalarkax;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bakalarkax.ApiService;
import com.example.bakalarkax.Clothes.ClothingItem;
import com.example.bakalarkax.Clothes.ClothingUsageTracker;
import com.example.bakalarkax.R;
import com.example.bakalarkax.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClothingDetailsDialog {

    public interface OnClothingAddListener {
        void onClothingAdded(ClothingItem item);
    }

    public static void show(Activity activity, ClothingItem item, boolean showPickButton, OnClothingAddListener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_clothing_details, null);
        dialog.setContentView(view);

        ImageView clothingImage = view.findViewById(R.id.clothingImage);
        TextView clothingName = view.findViewById(R.id.clothingName);
        TextView clothingBrand = view.findViewById(R.id.clothingBrand);
        Button addClothingButton = view.findViewById(R.id.addClothingButton);
        LinearLayout usageContainer = view.findViewById(R.id.usageContainer);

        clothingName.setText(item.getCategory());
        clothingBrand.setText(item.getBrand());
        Picasso.get().load(item.getImageUrl()).into(clothingImage);

        if (!showPickButton) {
            addClothingButton.setVisibility(View.GONE);
        } else {
            addClothingButton.setOnClickListener(v -> {
                if (listener != null) listener.onClothingAdded(item);
                dialog.dismiss();
            });
        }

        fetchClothingUsage(item.getIdClothing(), activity, usageContainer);
        dialog.show();
    }


    private static void fetchClothingUsage(int clothingId, Activity activity, LinearLayout usageContainer) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ClothingItem> call = apiService.getClothingUsage(clothingId);
        call.enqueue(new Callback<ClothingItem>() {
            @Override
            public void onResponse(Call<ClothingItem> call, Response<ClothingItem> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    List<String> rawDates = response.body().getDates();
                    Log.d("ClothingUsage", "Received worn dates: " + rawDates);
                    ClothingUsageTracker tracker = new ClothingUsageTracker(rawDates);
                    displayUsageHistory(tracker, usageContainer, activity);
                } else {
                    Log.e("ClothingUsage", "Response failed or empty");
                }
            }

            @Override
            public void onFailure(Call<ClothingItem> call, Throwable t) {
                Toast.makeText(activity, "Error loading usage data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void displayUsageHistory(ClothingUsageTracker tracker, LinearLayout usageContainer, Activity activity) {
        usageContainer.removeAllViews();
        int days = 14;
        List<Boolean> usage = tracker.getLastNDaysUsage(days);
        List<String> labels = tracker.getLastNDaysLabelsWithDates(days);

        for (int i = 0; i < days; i++) {
            View dayView = LayoutInflater.from(activity).inflate(R.layout.usage_history, null);

            TextView dayLabel = dayView.findViewById(R.id.dayLabel);
            View usageIndicator = dayView.findViewById(R.id.usageIndicator);

            dayLabel.setText(labels.get(i));
            usageIndicator.setBackgroundColor(
                    usage.get(i) ? activity.getColor(R.color.red) : activity.getColor(R.color.gray)
            );

            usageContainer.addView(dayView);
        }
    }

}

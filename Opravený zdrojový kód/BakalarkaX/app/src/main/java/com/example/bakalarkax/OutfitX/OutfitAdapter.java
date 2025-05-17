package com.example.bakalarkax.OutfitX;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakalarkax.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {
    private Context context;
    private List<Outfit> outfitList;

    public OutfitAdapter(Context context, List<Outfit> outfitList) {
        this.context = context;
        this.outfitList = outfitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_outfit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        holder.outfitName.setText(outfit.getOutfitName());
        String formattedDate = formatDate(outfit.getCreatedAt());
        holder.outfitDate.setText(formattedDate);


        holder.headContainer.removeAllViews();
        holder.upperContainer.removeAllViews();
        holder.lowerContainer.removeAllViews();
        holder.shoesContainer.removeAllViews();

        for (Outfit.ClothingItem clothing : outfit.getClothingItems()) {

            ImageView imageView = new ImageView(context);
            int width = calculateResponsiveImageWidth();
            int height = (width * 4) / 3;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(8, 8, 8, 8);

            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


            Picasso.get().load(clothing.getImageUrl()).into(imageView);

            switch (clothing.getType().toLowerCase()) {
                case "head":
                    holder.headContainer.addView(imageView);
                    Log.d("Clothing_Debug", "Added to HEAD container");
                    break;
                case "upper":
                    holder.upperContainer.addView(imageView);
                    Log.d("Clothing_Debug", "Added to UPPER container");
                    break;
                case "lower":
                    holder.lowerContainer.addView(imageView);
                    Log.d("Clothing_Debug", "Added to LOWER container");
                    break;
                case "shoes":
                    holder.shoesContainer.addView(imageView);
                    Log.d("Clothing_Debug", "Added to SHOES container");
                    break;
                default:
                    Log.e("Clothing_Debug", "Unknown clothing type: " + clothing.getType());
                    break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    public void updateList(List<Outfit> newList) {
        this.outfitList.clear();
        this.outfitList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView outfitName, outfitDate;
        LinearLayout headContainer, upperContainer, lowerContainer, shoesContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            outfitName = itemView.findViewById(R.id.textViewOutfitName);
            outfitDate = itemView.findViewById(R.id.textViewOutfitDate);
            headContainer = itemView.findViewById(R.id.headContainer);
            upperContainer = itemView.findViewById(R.id.upperContainer);
            lowerContainer = itemView.findViewById(R.id.lowerContainer);
            shoesContainer = itemView.findViewById(R.id.shoesContainer);
        }
    }
    private String formatDate(String inputDateTime) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(inputDateTime);

            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return inputDateTime;
        }
    }

    private int calculateResponsiveImageWidth() {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        return screenWidth / 5 - 55;
    }


}

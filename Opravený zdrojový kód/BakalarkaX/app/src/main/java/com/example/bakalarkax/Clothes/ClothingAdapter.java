package com.example.bakalarkax.Clothes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bakalarkax.R;

import java.util.List;


public class ClothingAdapter extends RecyclerView.Adapter<ClothingAdapter.ClothingViewHolder> {

    private Context context;
    private List<ClothingItem> clothingItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ClothingItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ClothingAdapter(Context context, List<ClothingItem> clothingItems) {
        this.context = context;
        this.clothingItems = clothingItems;
    }

    @NonNull
    @Override
    public ClothingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clothing, parent, false);
        return new ClothingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothingViewHolder holder, int position) {
        ClothingItem item = clothingItems.get(position);
        holder.textViewBrand.setText(item.getBrand());
        holder.textViewCategory.setText(item.getCategory());
        holder.textViewSeason.setText(item.getSeason());
        holder.textViewQrCode.setText("QR ID: " + item.getIdQr());

        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.imageViewClothing);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return clothingItems.size();
    }

    public static class ClothingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewClothing;
        TextView textViewBrand, textViewCategory, textViewSeason, textViewQrCode;

        public ClothingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewClothing = itemView.findViewById(R.id.imageViewClothing);
            textViewBrand = itemView.findViewById(R.id.textViewBrand);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewSeason = itemView.findViewById(R.id.textViewSeason);
            textViewQrCode = itemView.findViewById(R.id.textViewQrCode);
        }
    }

    public void updateList(List<ClothingItem> newList) {
        this.clothingItems = newList;
        notifyDataSetChanged();
    }


    public interface OnItemLongClickListener {
        void onItemLongClick(ClothingItem item);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

}

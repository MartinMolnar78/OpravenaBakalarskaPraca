package com.example.bakalarkax.Clothes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bakalarkax.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ClothingAdapter2 extends RecyclerView.Adapter<ClothingAdapter2.ClothingViewHolder2> {

    private Context context;
    private List<ClothingItem> clothingItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ClothingItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ClothingAdapter2(Context context, List<ClothingItem> clothingItems) {
        this.context = context;
        this.clothingItems = clothingItems;
    }

    @NonNull
    @Override
    public ClothingViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clothing_2, parent, false);
        return new ClothingViewHolder2(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClothingViewHolder2 holder, int position) {
        ClothingItem item = clothingItems.get(position);
        holder.textViewBrand.setText(item.getBrand());

        Picasso.get().load(item.getImageUrl()).into(holder.imageViewClothing);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clothingItems.size();
    }

    public static class ClothingViewHolder2 extends RecyclerView.ViewHolder {
        ImageView imageViewClothing;
        TextView textViewBrand;

        public ClothingViewHolder2(@NonNull View itemView) {
            super(itemView);
            imageViewClothing = itemView.findViewById(R.id.imageViewClothing);
            textViewBrand = itemView.findViewById(R.id.textViewBrand);
        }
    }
}

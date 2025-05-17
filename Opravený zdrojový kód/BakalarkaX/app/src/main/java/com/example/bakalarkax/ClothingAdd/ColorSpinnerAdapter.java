package com.example.bakalarkax.ClothingAdd;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bakalarkax.R;

import java.util.List;

public class ColorSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final List<ColorItem> colorItems;

    public ColorSpinnerAdapter(Context context, List<ColorItem> colorItems) {
        this.context = context;
        this.colorItems = colorItems;
    }

    @Override
    public int getCount() {
        return colorItems.size();
    }

    @Override
    public Object getItem(int position) {
        return colorItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createColorView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createColorView(position, convertView, parent);
    }

    private View createColorView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.spinner_color, parent, false);

        View colorDot = view.findViewById(R.id.color_dot);
        TextView colorName = view.findViewById(R.id.color_name); // <== pridanie TextView

        ColorItem item = colorItems.get(position);

        Drawable background = colorDot.getBackground().mutate();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(item.colorValue);
        } else {
            colorDot.setBackgroundColor(item.colorValue);
        }

        colorName.setText(item.name);

        return view;
    }

}

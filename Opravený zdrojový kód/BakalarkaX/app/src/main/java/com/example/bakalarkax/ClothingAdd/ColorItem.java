package com.example.bakalarkax.ClothingAdd;

public class ColorItem {
    public int colorValue;
    public String name;

    public ColorItem(int colorValue, String name) {
        this.colorValue = colorValue;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

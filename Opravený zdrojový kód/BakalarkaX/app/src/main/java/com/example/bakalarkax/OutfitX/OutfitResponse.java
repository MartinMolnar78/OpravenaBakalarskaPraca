package com.example.bakalarkax.OutfitX;

import java.util.List;

public class OutfitResponse {
    public boolean success;
    public List<Outfit> outfits;

    public boolean isSuccess() { return success; }
    public List<Outfit> getOutfits() { return outfits; }
}

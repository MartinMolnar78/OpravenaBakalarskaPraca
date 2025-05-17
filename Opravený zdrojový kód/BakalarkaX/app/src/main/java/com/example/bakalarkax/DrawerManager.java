package com.example.bakalarkax;

import android.content.Intent;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bakalarkax.Clothes.MyClothesActivity;
import com.example.bakalarkax.ClothingAdd.ClothingAddActivity;
import com.example.bakalarkax.OutfitX.OutfitAddActivity;
import com.example.bakalarkax.OutfitX.OutfitListActivity;
import com.example.bakalarkax.QRGenerator.GenerateQR;
import com.google.android.material.navigation.NavigationView;

public class DrawerManager {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private AppCompatActivity activity;

    public DrawerManager(AppCompatActivity activity, DrawerLayout drawerLayout, NavigationView navigationView, Toolbar toolbar) {
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toolbar = toolbar;
        this.activity = activity;

        setupDrawer(activity);
        activity.setTitle("Smartdrobe");

    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            Intent intent = new Intent(activity, DashboardActivity.class);
            activity.startActivity(intent);
            return true;
        } else if (id == R.id.menu_add_clothes) {
            Intent intent = new Intent(activity, ClothingAddActivity.class);
            activity.startActivity(intent);
            return true;
        } else if (id == R.id.menu_my_clothes) {
            Intent intent = new Intent(activity, MyClothesActivity.class);
            activity.startActivity(intent);
            return true;
        } else if(id==R.id.menu_outfit){
            Intent intent = new Intent(activity, OutfitAddActivity.class);
            activity.startActivity(intent);
            return true;
        }else if(id==R.id.menu_outfit_list){
            Intent intent = new Intent(activity, OutfitListActivity.class);
            activity.startActivity(intent);
            return true;
        }else if(id==R.id.generate_qr){
            Intent intent = new Intent(activity, GenerateQR.class);
            activity.startActivity(intent);
            return true;
        }else if (id == R.id.menu_logout) {
            showLogoutDialog();
            return true;
        }
        return false;
    }

    private void setupDrawer(AppCompatActivity activity) {
        activity.setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(item -> onNavigationItemSelected(item));
    }

    public void closeDrawerOnBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void showLogoutDialog() {
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    activity.getSharedPreferences("UserPrefs", activity.MODE_PRIVATE)
                            .edit()
                            .clear()
                            .apply();

                    Intent intent = new Intent(activity, MainActivity.class); // alebo LoginActivity ak ju máš samostatnú
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}

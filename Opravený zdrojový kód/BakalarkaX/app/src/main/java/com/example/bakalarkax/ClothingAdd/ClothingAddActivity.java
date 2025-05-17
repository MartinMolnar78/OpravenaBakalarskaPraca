package com.example.bakalarkax.ClothingAdd;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.bakalarkax.*;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClothingAddActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText clothingBrand;
    private Spinner spinnerType, spinnerCategory, spinnerSeason, spinnerColor;
    private Button submitButton, buttonQrCodeAdd;
    private File photoFile;
    private String qrCodeNumber = "0";
    private List<ColorItem> colorItemList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clothing_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        clothingBrand = findViewById(R.id.clothingBrand);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerSeason = findViewById(R.id.spinnerSeason);
        spinnerColor = findViewById(R.id.spinnerColor);
        submitButton = findViewById(R.id.buttonAddPhoto);
        buttonQrCodeAdd = findViewById(R.id.buttonQrCodeAdd);
        TextView textViewQrResult = findViewById(R.id.textViewQrResult);


        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        new DrawerManager(this, drawerLayout, navigationView, toolbar);

        colorItemList = Arrays.asList(
                new ColorItem(Color.BLACK, "Black"),
                new ColorItem(Color.DKGRAY, "Dark Gray"),
                new ColorItem(Color.GRAY, "Gray"),
                new ColorItem(Color.LTGRAY, "Light Gray"),
                new ColorItem(Color.WHITE, "White"),
                new ColorItem(Color.RED, "Red"),
                new ColorItem(Color.GREEN, "Green"),
                new ColorItem(Color.BLUE, "Blue"),
                new ColorItem(Color.YELLOW, "Yellow"),
                new ColorItem(Color.CYAN, "Cyan"),
                new ColorItem(Color.MAGENTA, "Magenta"),
                new ColorItem(Color.TRANSPARENT, "Transparent"),
                new ColorItem(Color.parseColor("#FFA500"), "Orange"),
                new ColorItem(Color.parseColor("#800080"), "Purple"),
                new ColorItem(Color.parseColor("#FFC0CB"), "Pink"),
                new ColorItem(Color.parseColor("#A52A2A"), "Brown"),
                new ColorItem(Color.parseColor("#008080"), "Teal"),
                new ColorItem(Color.parseColor("#808000"), "Olive"),
                new ColorItem(Color.parseColor("#00FFFF"), "Aqua")
        );
        ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(this, colorItemList);
        spinnerColor.setAdapter(colorAdapter);
        setupMainCategorySpinner();

        submitButton.setOnClickListener(v -> {
            if (isAnyFieldFilled()) {
                openCamera();
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            }
        });

        buttonQrCodeAdd.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan a QR code");
            integrator.setOrientationLocked(true);
            integrator.initiateScan();
        });
    }

    private void setupMainCategorySpinner() {
        ArrayAdapter<CharSequence> mainCategoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_main, android.R.layout.simple_spinner_item);
        mainCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(mainCategoryAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                int subCategoryArrayId = getSubCategoryArrayId(selectedCategory);
                if (subCategoryArrayId != 0) {
                    ArrayAdapter<CharSequence> subCategoryAdapter = ArrayAdapter.createFromResource(
                            ClothingAddActivity.this, subCategoryArrayId, android.R.layout.simple_spinner_item);
                    subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(subCategoryAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int getSubCategoryArrayId(String selectedCategory) {
        switch (selectedCategory) {
            case "Head": return R.array.subcategory_head;
            case "Upper": return R.array.subcategory_upper;
            case "Lower": return R.array.subcategory_lower;
            case "Shoes": return R.array.subcategory_shoes;
            default: return 0;
        }
    }

    private boolean isAnyFieldFilled() {
        return !clothingBrand.getText().toString().trim().isEmpty();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.bakalarkax.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String qrData = result.getContents();
            String qrNumber = parseQrNumber(qrData);
            if (qrNumber != null) {
                validateQrCode(qrNumber, qrData);
            } else {
                Toast.makeText(this, "Invalid QR code format!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, ReviewClothingActivity.class);
            intent.putExtra("photoFilePath", photoFile.getAbsolutePath());
            intent.putExtra("clothingBrand", clothingBrand.getText().toString());
            intent.putExtra("type", spinnerType.getSelectedItem().toString());
            intent.putExtra("category", spinnerCategory.getSelectedItem().toString());
            intent.putExtra("season", spinnerSeason.getSelectedItem().toString());
            ColorItem selectedColorItem = (ColorItem) spinnerColor.getSelectedItem();
            String colorName = selectedColorItem.name;
            intent.putExtra("color", colorName);
            intent.putExtra("color", colorName);
            intent.putExtra("idQr", qrCodeNumber);
            startActivity(intent);
        }
    }

    private String parseQrNumber(String qrData) {
        for (String field : qrData.split(",")) {
            String[] kv = field.split(":");
            if (kv.length == 2 && kv[0].trim().equalsIgnoreCase("number"))
                return kv[1].trim();
        }
        return null;
    }

    private void validateQrCode(String qrNumber, String qrData) {
        String userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("user_id", "");
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.isQrCodeAssigned(userId, qrNumber).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && Boolean.FALSE.equals(response.body())) {
                    qrCodeNumber = qrNumber;
                    TextView textViewQrResult = findViewById(R.id.textViewQrResult);
                    textViewQrResult.setText("QR ID: " + qrNumber);
                    processQRData(qrData);
                } else {
                    Toast.makeText(ClothingAddActivity.this, "QR code already assigned.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(ClothingAddActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void processQRData(String qrData) {
        for (String field : qrData.split(",")) {
            String[] kv = field.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().toLowerCase();
                String value = kv[1].trim();
                setQRValue(key, value);
            }
        }
    }

    private void setQRValue(String key, String value) {
        switch (key) {
            case "brand":
                clothingBrand.setText(value);
                break;
            case "type":
                setSpinnerValue("type", spinnerType, value);
                break;
            case "season":
                setSpinnerValue("season", spinnerSeason, value);
                break;
            case "color":
                setSpinnerValue("color", spinnerColor, value);
                break;
            case "category":
                setSpinnerValue("category", spinnerCategory, value);
                break;
            default:
                Log.d("QR_DEBUG", "Unknown key encountered: " + key);
                break;
        }
    }

    private void setSpinnerValue(String spinnerName, Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        boolean found = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            String spinnerValue = adapter.getItem(i).toString();
            if (spinnerValue.equalsIgnoreCase(value.trim())) {
                spinner.setSelection(i);
                found = true;
                break;
            }
        }
        if (!found) {
            spinner.setSelection(0);
        }
    }

}

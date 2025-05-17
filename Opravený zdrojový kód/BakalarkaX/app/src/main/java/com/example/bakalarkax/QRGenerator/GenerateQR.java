package com.example.bakalarkax.QRGenerator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bakalarkax.ClothingAdd.ColorItem;
import com.example.bakalarkax.ClothingAdd.ColorSpinnerAdapter;
import com.example.bakalarkax.DrawerManager;
import com.example.bakalarkax.R;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateQR extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generate_qr);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        EditText pdfNameEditText = findViewById(R.id.pdfName);
        EditText rangeFromEditText = findViewById(R.id.rangeFrom);
        EditText rangeToEditText = findViewById(R.id.rangeTo);
        Spinner spinnerType = findViewById(R.id.spinnerType);
        Spinner spinnerSeason = findViewById(R.id.spinnerSeason);
        Spinner spinnerColor = findViewById(R.id.spinnerColor);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        CheckBox checkBoxType = findViewById(R.id.checkBoxType);
        CheckBox checkBoxSeason = findViewById(R.id.checkBoxSeason);
        CheckBox checkBoxColor = findViewById(R.id.checkBoxColor);
        Button buttonGeneratePdf = findViewById(R.id.buttonGeneratePdf);

        List<ColorItem> colorItemList = Arrays.asList(
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

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_main, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(
                this, R.array.category_season, android.R.layout.simple_spinner_item);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeason.setAdapter(seasonAdapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                int subCategoryArrayId = getSubCategoryArrayId(selectedType);
                if (subCategoryArrayId != 0) {
                    ArrayAdapter<CharSequence> subCategoryAdapter = ArrayAdapter.createFromResource(
                            GenerateQR.this, subCategoryArrayId, android.R.layout.simple_spinner_item);
                    subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(subCategoryAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        buttonGeneratePdf.setOnClickListener(v -> {
            int rangeFrom;
            int rangeTo;
            try {
                rangeFrom = Integer.parseInt(rangeFromEditText.getText().toString());
                rangeTo = Integer.parseInt(rangeToEditText.getText().toString());
            } catch (NumberFormatException ex) {
                Toast.makeText(GenerateQR.this, "Enter a valid numeric range!", Toast.LENGTH_LONG).show();
                return;
            }

            String pdfNameInput = pdfNameEditText.getText().toString().trim();
            String pdfFileName = pdfNameInput.isEmpty() ? "qr_codes.pdf" : pdfNameInput + ".pdf";

            String type = checkBoxType.isChecked() ? spinnerType.getSelectedItem().toString() : "";
            String season = checkBoxSeason.isChecked() ? spinnerSeason.getSelectedItem().toString() : "";
            String color = checkBoxColor.isChecked() ? ((ColorItem) spinnerColor.getSelectedItem()).name : "";
            String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";

            List<Bitmap> qrCodeBitmaps = new ArrayList<>();
            for (int num = rangeFrom; num <= rangeTo; num++) {
                String qrContent = createQRContent(num, type, season, color, category);
                Bitmap qrBitmap = generateQRCode(qrContent, 300, 300);
                if (qrBitmap != null) {
                    qrCodeBitmaps.add(qrBitmap);
                }
            }

            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(downloadDir, pdfFileName);
            createPdfWithQRCodes(qrCodeBitmaps, pdfFile.getAbsolutePath());
            Toast.makeText(GenerateQR.this, "PDF has been generated: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        new DrawerManager(this, drawerLayout, navigationView, toolbar);
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

    private String createQRContent(int number, String type, String season, String color, String category) {
        StringBuilder content = new StringBuilder();
        content.append("number:").append(number);
        if (!type.isEmpty()) content.append(", type:").append(type);
        if (!category.isEmpty()) content.append(", category:").append(category);
        if (!season.isEmpty()) content.append(", season:").append(season);
        if (!color.isEmpty()) content.append(", color:").append(color);
        return content.toString();
    }

    private Bitmap generateQRCode(String content, int width, int height) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createPdfWithQRCodes(List<Bitmap> qrBitmaps, String filePath) {
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument pdfDocument = new PdfDocument();

        int columns = 2;
        int rows = 4;
        int itemsPerPage = columns * rows;
        Paint paint = new Paint();
        int pageNumber = 0;

        for (int i = 0; i < qrBitmaps.size(); i += itemsPerPage) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            int xOffset = 20;
            int yOffset = 20;
            int qrWidth = (pageWidth - (columns + 1) * xOffset) / columns;
            int qrHeight = qrWidth;

            for (int j = 0; j < itemsPerPage && (i + j) < qrBitmaps.size(); j++) {
                Bitmap qrBitmap = qrBitmaps.get(i + j);
                int col = j % columns;
                int row = j / columns;

                int left = xOffset + col * (qrWidth + xOffset);
                int top = yOffset + row * (qrHeight + yOffset);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(qrBitmap, qrWidth, qrHeight, false);
                canvas.drawBitmap(scaledBitmap, left, top, paint);
            }

            pdfDocument.finishPage(page);
            pageNumber++;
        }

        try {
            File file = new File(filePath);
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission to write to external storage is required!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

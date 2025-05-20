package com.example.mysportik;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yandex.mapkit.MapKitFactory;
import com.example.mysportik.databinding.ActivityMapMarksBinding;


public class MapMarksActivity extends AppCompatActivity {
    private ActivityMapMarksBinding binding;
    private static final String MAPKIT_API_KEY = "0f9cf834-949f-4b43-b614-25dc4b4b47f6";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setApiKey(savedInstanceState); // Проверяем и устанавливаем API-ключ
        MapKitFactory.initialize(this); // Инициализация MapKit
        binding = ActivityMapMarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void setApiKey(Bundle savedInstanceState) {
        boolean haveApiKey = savedInstanceState != null && savedInstanceState.getBoolean("haveApiKey");
        if (!haveApiKey) {
            MapKitFactory.setApiKey(MAPKIT_API_KEY);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("haveApiKey", true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        binding.mapview.onStart();
    }

    @Override
    protected void onStop() {
        binding.mapview.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}
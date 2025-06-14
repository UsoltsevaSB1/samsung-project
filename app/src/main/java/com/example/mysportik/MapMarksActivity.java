package com.example.mysportik;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.MapKitFactory;
import com.example.mysportik.databinding.ActivityMapMarksBinding;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;

import android.os.Bundle;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import java.util.ArrayList;
import java.util.List;


import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;

import java.util.Locale;



public class MapMarksActivity extends AppCompatActivity {
    private ActivityMapMarksBinding binding;
    private static final String MAPKIT_API_KEY = "0f9cf834-949f-4b43-b614-25dc4b4b47f6";
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        binding = ActivityMapMarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Получаем данные метки из Intent
        if (getIntent() != null && getIntent().hasExtra("marker")) {
            currentMarker = getIntent().getParcelableExtra("marker");
            initializeMapWithMarker();
        }
        ImageButton backButton = findViewById(R.id.backButton1);
        // Обработчик клика
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем Intent для перехода на LoginActivity
                Intent intent = new Intent(MapMarksActivity.this, UserMarksActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeMapWithMarker() {
        binding.getRoot().post(() -> {
            if (binding.mapview.getMap() != null) {
                setupMap();
            } else {
                binding.getRoot().postDelayed(this::setupMap, 300);
            }
        });
    }

    private void setupMap() {
        // Очищаем старые метки
        binding.mapview.getMap().getMapObjects().clear();

        // Добавляем новую метку
        Point point = new Point(currentMarker.getLatitude(), currentMarker.getLongitude());
        PlacemarkMapObject placemark = binding.mapview.getMap().getMapObjects().addPlacemark(point);

        // Настраиваем внешний вид метки
        placemark.setOpacity(25.0f);
        placemark.setDraggable(false);

        // Увеличиваем размер метки
        IconStyle iconStyle = new IconStyle();
        iconStyle.setScale(2.0f); // 2.0 делает метку в 2 раза больше
        placemark.setIconStyle(iconStyle);

        // Обработчик нажатия на метку
        placemark.addTapListener((mapObject, point1) -> {
            showMarkerInfo(currentMarker);
            return true;
        });

        // Центрируем карту на метке с увеличенным зумом
        binding.mapview.getMap().move(
                new CameraPosition(point, 18f, 0f, 0f), // Увеличили зум до 18
                new Animation(Animation.Type.SMOOTH, 0.5f),
                null
        );
    }

    private void showMarkerInfo(Marker marker) {
        new AlertDialog.Builder(this)
                .setTitle(marker.getName())
                .setMessage(formatMarkerInfo(marker))
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatMarkerInfo(Marker marker) {
        String note = marker.getNote();
        if (note == null || note.isEmpty()) {
            note = "Нет заметки";
        }
        return String.format(Locale.getDefault(),
                "Координаты: %.6f, %.6f\n\nЗаметка: %s",
                marker.getLatitude(),
                marker.getLongitude(),
                note);
    }

    // Остальные методы жизненного цикла остаются без изменений
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







//public class MapMarksActivity extends AppCompatActivity {
//    private ActivityMapMarksBinding binding;
//    private static final String MAPKIT_API_KEY = "0f9cf834-949f-4b43-b614-25dc4b4b47f6";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setApiKey(savedInstanceState); // Проверяем и устанавливаем API-ключ
//        MapKitFactory.initialize(this); // Инициализация MapKit
//        binding = ActivityMapMarksBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//    }
//
//    private void setApiKey(Bundle savedInstanceState) {
//        boolean haveApiKey = savedInstanceState != null && savedInstanceState.getBoolean("haveApiKey");
//        if (!haveApiKey) {
//            MapKitFactory.setApiKey(MAPKIT_API_KEY);
//        }
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean("haveApiKey", true);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        MapKitFactory.getInstance().onStart();
//        binding.mapview.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        binding.mapview.onStop();
//        MapKitFactory.getInstance().onStop();
//        super.onStop();
//    }}

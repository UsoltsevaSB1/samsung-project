package com.example.mysportik;
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
import androidx.appcompat.app.AppCompatActivity;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
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
    private List<Marker> markers = new ArrayList<>();
    private double centerLat;
    private double centerLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapMarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Получаем данные из Intent
        if (getIntent() != null) {
            markers = getIntent().getParcelableArrayListExtra("markers_list");
            centerLat = getIntent().getDoubleExtra("center_lat", 0);
            centerLon = getIntent().getDoubleExtra("center_lon", 0);
        }

        // Инициализация карты с небольшой задержкой
        binding.getRoot().post(() -> {
            if (binding.mapview.getMap() != null) {
                initializeMap();
            } else {
                        // Для Яндекс.Карт просто ждем, пока карта инициализируется

                        binding.getRoot().postDelayed(this::initializeMap, 300);
                    }
                });
    }

    private void initializeMap() {
        // Очищаем старые метки
        binding.mapview.getMap().getMapObjects().clear();

        // Добавляем новые метки
        for (Marker marker : markers) {
            addMarkerToMap(marker);
        }

        // Центрируем карту
        centerMap();
    }

    private void addMarkerToMap(Marker marker) {
        Point point = new Point(marker.getLatitude(), marker.getLongitude());
        PlacemarkMapObject placemark = binding.mapview.getMap().getMapObjects().addPlacemark(point);

        // Настройка внешнего вида
        placemark.setOpacity(0.8f);
        placemark.setDraggable(false);

        // Обработчик нажатия
        placemark.addTapListener((mapObject, point1) -> {
            showMarkerInfo(marker);
            return true;
        });
    }

    private void centerMap() {
        Point center = new Point(centerLat, centerLon);
        binding.mapview.getMap().move(
                new CameraPosition(center, 12f, 0f, 0f),
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
        return String.format(Locale.getDefault(),
                "ID: %s\nШирота: %.6f\nДолгота: %.6f",
                marker.getId(),
                marker.getLatitude(),
                marker.getLongitude());
    }

    // Остальные методы остаются без изменений
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

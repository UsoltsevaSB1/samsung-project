//package com.example.mysportik;
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.yandex.mapkit.MapKitFactory;
//import com.example.mysportik.databinding.ActivityMapMarksBinding;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.app.AlertDialog;
//import com.yandex.mapkit.geometry.Point;
//import com.yandex.mapkit.map.CameraPosition;
//
//import android.os.Bundle;
//import android.app.AlertDialog;
//import android.view.View;
//import android.widget.ImageButton;
//
//import androidx.appcompat.app.AppCompatActivity;
//import com.yandex.mapkit.Animation;
//import com.yandex.mapkit.MapKitFactory;
//import com.yandex.mapkit.geometry.Point;
//import com.yandex.mapkit.map.CameraPosition;
//import com.yandex.mapkit.map.CameraUpdateReason;
//import com.yandex.mapkit.map.IconStyle;
//import com.yandex.mapkit.map.MapObjectCollection;
//import com.yandex.mapkit.map.PlacemarkMapObject;
//import com.yandex.mapkit.mapview.MapView;
//import java.util.ArrayList;
//import java.util.List;
//
//
//import com.yandex.mapkit.map.CameraPosition;
//import com.yandex.mapkit.map.CameraUpdateReason;
//
//import java.util.Locale;
//
//
//
//public class MapMarksActivity extends AppCompatActivity {
//    private ActivityMapMarksBinding binding;
//    private static final String MAPKIT_API_KEY = "0f9cf834-949f-4b43-b614-25dc4b4b47f6";
//    private Marker currentMarker;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        MapKitFactory.setApiKey(MAPKIT_API_KEY);
//        MapKitFactory.initialize(this);
//        binding = ActivityMapMarksBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // Получаем данные метки из Intent
//        if (getIntent() != null && getIntent().hasExtra("marker")) {
//            currentMarker = getIntent().getParcelableExtra("marker");
//            initializeMapWithMarker();
//        }
//        ImageButton backButton = findViewById(R.id.backButton1);
//        // Обработчик клика
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Создаем Intent для перехода на LoginActivity
//                Intent intent = new Intent(MapMarksActivity.this, UserMarksActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//    }
//
//    private void initializeMapWithMarker() {
//        binding.getRoot().post(() -> {
//            if (binding.mapview.getMap() != null) {
//                setupMap();
//            } else {
//                binding.getRoot().postDelayed(this::setupMap, 300);
//            }
//        });
//    }
//
//    private void setupMap() {
//        // Очищаем старые метки
//        binding.mapview.getMap().getMapObjects().clear();
//
//        // Добавляем новую метку
//        Point point = new Point(currentMarker.getLatitude(), currentMarker.getLongitude());
//        PlacemarkMapObject placemark = binding.mapview.getMap().getMapObjects().addPlacemark(point);
//
//        // Настраиваем внешний вид метки
//        placemark.setOpacity(25.0f);
//        placemark.setDraggable(false);
//
//        // Увеличиваем размер метки
//        IconStyle iconStyle = new IconStyle();
//        iconStyle.setScale(2.0f); // 2.0 делает метку в 2 раза больше
//        placemark.setIconStyle(iconStyle);
//
//        // Обработчик нажатия на метку
//        placemark.addTapListener((mapObject, point1) -> {
//            showMarkerInfo(currentMarker);
//            return true;
//        });
//
//        // Центрируем карту на метке с увеличенным зумом
//        binding.mapview.getMap().move(
//                new CameraPosition(point, 18f, 0f, 0f), // Увеличили зум до 18
//                new Animation(Animation.Type.SMOOTH, 0.5f),
//                null
//        );
//    }
//
//    private void showMarkerInfo(Marker marker) {
//        new AlertDialog.Builder(this)
//                .setTitle(marker.getName())
//                .setMessage(formatMarkerInfo(marker))
//                .setPositiveButton("OK", null)
//                .show();
//    }
//
//    private String formatMarkerInfo(Marker marker) {
//        String note = marker.getNote();
//        if (note == null || note.isEmpty()) {
//            note = "Нет заметки";
//        }
//        return String.format(Locale.getDefault(),
//                "Координаты: %.6f, %.6f\n\nЗаметка: %s",
//                marker.getLatitude(),
//                marker.getLongitude(),
//                note);
//    }
//
//    // Остальные методы жизненного цикла остаются без изменений
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
//    }
//}







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
package com.example.mysportik;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.mapview.MapView;
import java.util.HashMap;

public class MapMarksActivity extends AppCompatActivity {
    private MapView mapView;
    private Button btnAddMarker, btnAddNote;
    private boolean isMarkerMode = false;
    private boolean isNoteMode = false;
    private final HashMap<MapObject, String> markerNotes = new HashMap<>();
    private MapObject selectedMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация Yandex MapKit
        MapKitFactory.setApiKey("4f7c3577-6bf8-4a85-9d77-552f8d759852");
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_map_marks);


        mapView = findViewById(R.id.mapview);
        btnAddMarker = findViewById(R.id.btn_add_marker);
        btnAddNote = findViewById(R.id.btn_add_note);

        // Установка начальной позиции карты (Москва)
        mapView.getMap().move(
                new CameraPosition(new Point(55.751574, 37.573856), 10, 0, 0)
        );

        // Обработчики кнопок
        btnAddMarker.setOnClickListener(v -> {
            isMarkerMode = !isMarkerMode;
            isNoteMode = false;
            updateButtonStates();
            updateMapListeners();
        });

        btnAddNote.setOnClickListener(v -> {
            isNoteMode = !isNoteMode;
            isMarkerMode = false;
            updateButtonStates();
            updateMapListeners();
        });

        updateButtonStates();
    }

    private void updateButtonStates() {
        btnAddNote.setVisibility(markerNotes.isEmpty() ? View.GONE : View.VISIBLE);
        btnAddMarker.setBackgroundColor(isMarkerMode ? 0xFF00FF00 : 0xFFFFFFFF);
        btnAddNote.setBackgroundColor(isNoteMode ? 0xFF00FF00 : 0xFFFFFFFF);
    }

    // 1. Сначала объявите поле класса для хранения слушателя
    private InputListener mapInputListener;

    // 2. В методе updateMapListeners() измените код:
    private void updateMapListeners() {
        // Удаляем предыдущий слушатель, если он был
        if (mapInputListener != null) {
            mapView.getMap().removeInputListener(mapInputListener);
        }

        if (isMarkerMode) {
            mapInputListener = new InputListener() {
                @Override
                public void onMapTap(Map map, Point point) {
                    addMarker(point);
                }

                @Override
                public void onMapLongTap(Map map, Point point) {
                    // Не используется
                }
            };
            mapView.getMap().addInputListener(mapInputListener);
        }
    }

    //    private void addMarker(Point point) {
//        // Для версий MapKit 4.0+
//        PlacemarkMapObject marker = mapView.getMap().getMapObjects().addPlacemark(
//                point,
//                ImageProvider.fromResource(this, R.drawable.ic_map_marker),
//                new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(1.5f)
//        );
//
//        setupMarkerTapListener(marker);
//        btnAddNote.setVisibility(View.VISIBLE);
//    }
    private void addMarker(Point point) {
        // Создаем метку с минимальным размером
        PlacemarkMapObject marker = mapView.getMap().getMapObjects().addPlacemark(point);

        // Настройка стиля для маленькой точки
        IconStyle iconStyle = new IconStyle()
                .setAnchor(new PointF(0.5f, 0.5f)) // Центр точки
                .setScale(0.05f); // Масштаб (0.5 = 50% от исходного размера)

        // Установка иконки (либо ваш маленький PNG, либо вектор)
        marker.setIcon(
                ImageProvider.fromResource(this, R.drawable.ic_map_marker),
                iconStyle
        );

        setupMarkerTapListener(marker);
        btnAddNote.setVisibility(View.VISIBLE);
    }

    private void setupMarkerTapListener(MapObject marker) {
        marker.addTapListener((mapObject, point) -> {
            if (isNoteMode) {
                selectedMarker = mapObject;
                showNoteDialog();
            }
            return true;
        });
    }

    private void showNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить заметку");

        final EditText input = new EditText(this);
        String existingNote = markerNotes.get(selectedMarker);
        if (existingNote != null) {
            input.setText(existingNote);
        }
        builder.setView(input);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String note = input.getText().toString();
            markerNotes.put(selectedMarker, note);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
}
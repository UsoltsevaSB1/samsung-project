package com.example.mysportik;

import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MapMarksActivity extends AppCompatActivity {
    private MapView mapView;
    private Button btnAddMarker, btnAddNote;
    private boolean isMarkerMode = false;
    private boolean isNoteMode = false;
    private final HashMap<MapObject, String> markerNotes = new HashMap<>();
    private MapObject selectedMarker = null;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private List<PlacemarkMapObject> markers = new ArrayList<>();

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Инициализация Firebase
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mAuth = FirebaseAuth.getInstance();
//
//        // Инициализация Yandex MapKit
//        MapKitFactory.setApiKey("4f7c3577-6bf8-4a85-9d77-552f8d759852");
//        MapKitFactory.initialize(this);
//        setContentView(R.layout.activity_map_marks);
//
//
//        mapView = findViewById(R.id.mapview);
//        btnAddMarker = findViewById(R.id.btn_add_marker);
//        btnAddNote = findViewById(R.id.btn_add_note);
//
//        // Установка начальной позиции карты (Москва)
//        mapView.getMap().move(
//                new CameraPosition(new Point(55.751574, 37.573856), 10, 0, 0)
//        );
//
//        // Обработчики кнопок
//        btnAddMarker.setOnClickListener(v -> {
//            isMarkerMode = !isMarkerMode;
//            isNoteMode = false;
//            updateButtonStates();
//            updateMapListeners();
//        });
//
//        btnAddNote.setOnClickListener(v -> {
//            isNoteMode = !isNoteMode;
//            isMarkerMode = false;
//            updateButtonStates();
//            updateMapListeners();
//        });
//
//        updateButtonStates();
//    }
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Инициализация Firebase
    mDatabase = FirebaseDatabase.getInstance().getReference();
    mAuth = FirebaseAuth.getInstance();

    // Инициализация Yandex MapKit
    MapKitFactory.setApiKey("4f7c3577-6bf8-4a85-9d77-552f8d759852");
    MapKitFactory.initialize(this);
    setContentView(R.layout.activity_map_marks);

    // Инициализация элементов UI
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

    // Добавляем слушатель изменения масштаба
    mapView.getMap().addCameraListener((map, cameraPosition, cameraUpdateReason, finished) -> {
        if (finished) {
            updateMarkersVisibility();
        }
    });

    updateButtonStates();
    // Начальное обновление видимости меток
    updateMarkersVisibility();
}

    private void updateMarkersVisibility() {
        try {
            float currentZoom = mapView.getMap().getCameraPosition().getZoom();
            float minZoom = 10f;
            float maxZoom = 19f;

            for (PlacemarkMapObject marker : markers) {
                boolean shouldBeVisible = currentZoom >= minZoom && currentZoom <= maxZoom;
                marker.setVisible(shouldBeVisible);

                if (shouldBeVisible) {
                    float scale = 0.5f + (currentZoom - 10) * 0.1f;
                    marker.setIconStyle(new IconStyle()
                            .setScale(scale)
                            .setAnchor(new PointF(0.5f, 1.0f))
                    );
                }
            }
        } catch (Exception e) {
            Log.e("MapError", "Ошибка обновления меток", e);
        }
    }


//    private void updateSingleMarker(PlacemarkMapObject marker, float currentZoom, float minZoom, float maxZoom) {
//        boolean shouldBeVisible = currentZoom >= minZoom && currentZoom <= maxZoom;
//        marker.setVisible(shouldBeVisible);
//
//        if (shouldBeVisible) {
//            float scale = calculateMarkerScale(currentZoom);
//            marker.setIconStyle(new IconStyle()
//                    .setScale(scale)
//                    .setAnchor(new PointF(0.5f, 1.0f))
//            );
//        }
//    }

    private float calculateMarkerScale(float zoom) {
        return 0.5f + (zoom - 10) * 0.1f; // Пример: масштаб от 0.5 до 1.5
    }

    private void addMarker(Point point) {
        PlacemarkMapObject marker = mapView.getMap().getMapObjects().addPlacemark(point);
        markers.add(marker); // Добавляем маркер в список

        // Начальные стили
        IconStyle iconStyle = new IconStyle()
                .setAnchor(new PointF(0.5f, 1.0f))
                .setScale(calculateMarkerScale(mapView.getMap().getCameraPosition().getZoom()));

        marker.setIcon(
                ImageProvider.fromResource(this, R.drawable.ic_map_marker),
                iconStyle
        );

        // Анимация появления
        marker.setOpacity(0f);
        ValueAnimator fadeAnim = ValueAnimator.ofFloat(0f, 1f);
        fadeAnim.setDuration(300);
        fadeAnim.addUpdateListener(animation -> {
            marker.setOpacity((float) animation.getAnimatedValue());
        });
        fadeAnim.start();

        // Обработчик клика
        marker.addTapListener((mapObject, pt) -> {
            if (isNoteMode) {
                selectedMarker = mapObject;
                showNoteDialog(((PlacemarkMapObject) mapObject).getGeometry());
            }
            return true;
        });

        btnAddNote.setVisibility(View.VISIBLE);
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

    private void setupMarkerTapListener(MapObject marker) {
        marker.addTapListener((mapObject, point) -> {
            if (isNoteMode) {
                selectedMarker = mapObject;
                // Получаем координаты метки
                Point markerPosition = ((PlacemarkMapObject) mapObject).getGeometry();
                // Передаем координаты в диалог
                showNoteDialog(markerPosition);
            }
            return true;
        });
    }
    private void showNoteDialog(Point point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mark_info, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_mark_name);
        EditText etNote = dialogView.findViewById(R.id.et_mark_note);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_mark_status);
        RadioButton rbPublic = dialogView.findViewById(R.id.rb_public);

        // Установка статуса по умолчанию
        rbPublic.setChecked(true);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            String status = rgStatus.getCheckedRadioButtonId() == R.id.rb_public ? "public" : "private";

            // Создание объекта Marker
            Marker marker = new Marker();
            marker.setLatitude(point.getLatitude());
            marker.setLongitude(point.getLongitude());
            marker.setName(name.isEmpty() ? "Без названия" : name);
            marker.setNote(note);
            marker.setStatus(status);
            marker.setTimestamp(System.currentTimeMillis());

            // Генерация геохеша (точность 8 символов)
            marker.setGeohash(GeoHashConverter.encode(
                    point.getLatitude(),
                    point.getLongitude(),
                    8
            ));

            // Установка ID пользователя
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                marker.setUserId(user.getUid());
            }

            // Сохранение в Firebase
            saveMarkerToFirebase(marker);
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void saveMarkerToFirebase(Marker marker) {
        // Генерация уникального ключа
        String key = mDatabase.child("Marks").push().getKey();
        if (key == null) return;

        // Создаем HashMap только с нужными полями (без id)
        HashMap<String, Object> markerData = new HashMap<>();
        markerData.put("latitude", marker.getLatitude());
        markerData.put("longitude", marker.getLongitude());
        markerData.put("name", marker.getName());
        markerData.put("note", marker.getNote());
        markerData.put("status", marker.getStatus());
        markerData.put("userId", marker.getUserId());
        markerData.put("timestamp", marker.getTimestamp());
        markerData.put("geohash", marker.getGeohash());

        // Сохранение в структуре Marks/{markerId}
        mDatabase.child("Marks").child(key).setValue(markerData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Метка сохранена", Toast.LENGTH_SHORT).show();

                    // Обновляем видимость кнопки заметок
                    btnAddNote.setVisibility(View.VISIBLE);

                    // Сбрасываем режимы
                    isMarkerMode = false;
                    isNoteMode = false;
                    updateButtonStates();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateMarkersVisibility();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateMarkersVisibility();
        }
    }
}
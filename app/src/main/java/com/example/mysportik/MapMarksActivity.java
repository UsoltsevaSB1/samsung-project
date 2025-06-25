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

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

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
                // Получаем координаты метки
                Point markerPosition = ((PlacemarkMapObject) mapObject).getGeometry();
                // Передаем координаты в диалог
                showNoteDialog(markerPosition);
            }
            return true;
        });
    }

    /*private void showNoteDialog() {
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
    }*/

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

    /*private void saveMarkerToFirebase(Marker marker) {
        // Генерация уникального ключа
        String key = mDatabase.child("Marks").push().getKey();
        if (key == null) return;

        // Установка ID маркера
        marker.setId(key);

        // Создаем HashMap со всеми полями
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
        mDatabase.child("Marks").child(key).setValue(marker)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Метка сохранена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }*/

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
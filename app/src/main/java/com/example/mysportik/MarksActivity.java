package com.example.mysportik;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mysportik.databinding.ActivityMarksBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarksActivity extends AppCompatActivity {

    private ActivityMarksBinding binding;
    private EditText center_latitude2, center_height2;
    private DatabaseReference marksRef;
    private DatabaseReference geohashIndexRef;
    private FirebaseAuth mAuth;
    private GeohashTable geohashTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        marksRef = FirebaseDatabase.getInstance().getReference("Marks");
        geohashIndexRef = FirebaseDatabase.getInstance().getReference("GeohashIndex");
        geohashTable = new GeohashTable(7);

        // Инициализация UI элементов
        center_latitude2 = findViewById(R.id.center_latitude2);
        center_height2 = findViewById(R.id.center_height2);
        Button find_marks = findViewById(R.id.find_marks);
        Button find_marks2 = findViewById(R.id.find_marks2);

        // Обработчики кнопок
        find_marks.setOnClickListener(v -> showRadiusSearchDialog());
        find_marks2.setOnClickListener(v -> {
            startActivity(new Intent(MarksActivity.this, UserMarksActivity.class));
        });
    }

    private void showRadiusSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поиск меток в радиусе");

        // Создаем кастомный View для диалога
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_radius_search, null);
        EditText radiusInput = dialogView.findViewById(R.id.radius_input);
        EditText latInput = dialogView.findViewById(R.id.lat_input);
        EditText lonInput = dialogView.findViewById(R.id.lon_input);

        // Устанавливаем текущие координаты из полей ввода
        latInput.setText(center_latitude2.getText().toString());
        lonInput.setText(center_height2.getText().toString());

        builder.setView(dialogView);

        builder.setPositiveButton("Искать", (dialog, which) -> {
            try {
                double lat = Double.parseDouble(latInput.getText().toString());
                double lon = Double.parseDouble(lonInput.getText().toString());
                double radius = Double.parseDouble(radiusInput.getText().toString());

                searchMarkersInRadius(lat, lon, radius);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Проверьте правильность ввода данных", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void searchMarkersInRadius(double centerLat, double centerLon, double radiusKm) {
        // Определяем оптимальную точность геохеша
        int precision = Marker.calculateOptimalPrecision(radiusKm);
        String centerGeohash = GeoHashConverter.encode(centerLat, centerLon, precision);

        // Получаем соседние геохеши
        List<String> neighborGeohashes = Marker.getNeighboringGeohashes(centerGeohash);
        neighborGeohashes.add(centerGeohash);

        // Создаем список для хранения результатов
        List<Marker> foundMarkers = new ArrayList<>();
        List<Task<DataSnapshot>> tasks = new ArrayList<>();

        // Запрашиваем метки для каждого геохеша
        for (String geohash : neighborGeohashes) {
            Task<DataSnapshot> task = geohashIndexRef.child(geohash).get()
                    .addOnSuccessListener(snapshot -> {
                        for (DataSnapshot markSnapshot : snapshot.getChildren()) {
                            Marker marker = markSnapshot.getValue(Marker.class);
                            if (marker != null) {
                                // Проверяем точное расстояние
                                if (Marker.calculateDistance(centerLat, centerLon,
                                        marker.getLatitude(), marker.getLongitude()) <= radiusKm) {
                                    foundMarkers.add(marker);
                                }
                            }
                        }
                    });
            tasks.add(task);
        }

        // Когда все запросы завершены
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (foundMarkers.isEmpty()) {
                Toast.makeText(this, "Меток в радиусе " + radiusKm + " км не найдено",
                        Toast.LENGTH_SHORT).show();
            } else {
                openMapWithMarkers(foundMarkers, centerLat, centerLon);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка поиска: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void openMapWithMarkers(List<Marker> markers, double centerLat, double centerLon) {
        Intent intent = new Intent(this, MapMarksActivity.class);
        intent.putExtra("center_lat", centerLat);
        intent.putExtra("center_lon", centerLon);
        intent.putExtra("markers", new ArrayList<>(markers));
        startActivity(intent);
    }

    public void saveMarker(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String marker_text = binding.markerText.getText().toString().trim();
        String center_latitude2 = binding.centerLatitude2.getText().toString().trim();
        String center_height2 = binding.centerHeight2.getText().toString().trim();
        String center_name = binding.centerName.getText().toString().trim();
        boolean isPublic = binding.publicMarker.isChecked();

        if (marker_text.isEmpty() || center_latitude2.isEmpty() || center_name.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double lat = Double.parseDouble(center_latitude2);
            double lon = Double.parseDouble(center_height2);

            String geohash = GeoHashConverter.encode(lat, lon, 7);

            HashMap<String, Object> marker = new HashMap<>();
            marker.put("name", center_name);
            marker.put("latitude", lat);
            marker.put("longitude", lon);
            marker.put("geohash", geohash);
            marker.put("status", isPublic ? "public" : "private");
            marker.put("userId", currentUser.getUid());
            marker.put("note", marker_text);
            marker.put("timestamp", ServerValue.TIMESTAMP);

            String markerId = marksRef.push().getKey();

            marksRef.child(markerId).setValue(marker)
                    .addOnCompleteListener(mainTask -> {
                        if (mainTask.isSuccessful()) {
                            Map<String, Object> indexEntry = new HashMap<>(marker);
                            indexEntry.remove("geohash");

                            geohashIndexRef.child(geohash).child(markerId)
                                    .setValue(indexEntry)
                                    .addOnCompleteListener(indexTask -> {
                                        if (indexTask.isSuccessful()) {
                                            geohashTable.addMarker(lat, lon, marker);
                                            runOnUiThread(() -> {
                                                Toast.makeText(MarksActivity.this,
                                                        "Метка успешно сохранена",
                                                        Toast.LENGTH_SHORT).show();
                                                clearFields();
                                            });
                                        } else {
                                            marksRef.child(markerId).removeValue();
                                            runOnUiThread(() ->
                                                    Toast.makeText(MarksActivity.this,
                                                            "Ошибка сохранения индекса: " + indexTask.getException().getMessage(),
                                                            Toast.LENGTH_LONG).show()
                                            );
                                        }
                                    });
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(MarksActivity.this,
                                            "Ошибка сохранения метки: " + mainTask.getException().getMessage(),
                                            Toast.LENGTH_LONG).show()
                            );
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Неверный формат координат", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Неизвестная ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void clearFields() {
        binding.markerText.setText("");
        binding.centerLatitude2.setText("");
        binding.centerHeight2.setText("");
        binding.centerName.setText("");
        binding.publicMarker.setChecked(false);
    }
}//package com.example.mysportik;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.RadioButton;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.mysportik.databinding.ActivityMarksBinding;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ServerValue;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import android.widget.ImageButton;
//import android.view.View;
//import android.content.Intent;
//
//
//
//public class MarksActivity extends AppCompatActivity {
//
//    private GeohashStorage storage;
//    private GeohashTable geohashTable;
//    EditText center_name, center_height2, center_latitude2, marker_text;
//    RadioButton public_marker;
//
//
//    private ActivityMarksBinding binding;
//    private FirebaseAuth mAuth;
//    private DatabaseReference mDatabase;
//    //private DatabaseReference markersRef;
//
//    private DatabaseReference marksRef;
//    private DatabaseReference geohashIndexRef;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityMarksBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // Инициализация Firebase
//        mAuth = FirebaseAuth.getInstance();
//        marksRef = FirebaseDatabase.getInstance().getReference("Marks");
//        geohashIndexRef = FirebaseDatabase.getInstance().getReference("GeohashIndex");
//        geohashTable = new GeohashTable(7);
//
//        // Инициализация UI элементов
//        center_latitude2 = findViewById(R.id.center_latitude2);
//        center_height2 = findViewById(R.id.center_height2);
//        Button find_marks = findViewById(R.id.find_marks);
//        Button find_marks2 = findViewById(R.id.find_marks2);
//
//        // Обработчики кнопок
//        find_marks.setOnClickListener(v -> showRadiusSearchDialog());
//        find_marks2.setOnClickListener(v -> {
//            startActivity(new Intent(MarksActivity.this, UserMarksActivity.class));
//        });
//    }
//
//    private void showRadiusSearchDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Поиск меток в радиусе");
//
//        // Создаем кастомный View для диалога
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_radius_search, null);
//        EditText radiusInput = dialogView.findViewById(R.id.radius_input);
//        EditText latInput = dialogView.findViewById(R.id.lat_input);
//        EditText lonInput = dialogView.findViewById(R.id.lon_input);
//
//        // Устанавливаем текущие координаты из полей ввода
//        latInput.setText(center_latitude2.getText().toString());
//        lonInput.setText(center_height2.getText().toString());
//
//        builder.setView(dialogView);
//
//        builder.setPositiveButton("Искать", (dialog, which) -> {
//            try {
//                double lat = Double.parseDouble(latInput.getText().toString());
//                double lon = Double.parseDouble(lonInput.getText().toString());
//                double radius = Double.parseDouble(radiusInput.getText().toString());
//
//                searchMarkersInRadius(lat, lon, radius);
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Проверьте правильность ввода данных", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.setNegativeButton("Отмена", null);
//        builder.show();
//    }
//
//    private void searchMarkersInRadius(double centerLat, double centerLon, double radiusKm) {
//        // Определяем оптимальную точность геохеша
//        int precision = Marker.calculateOptimalPrecision(radiusKm);
//        String centerGeohash = GeoHashConverter.encode(centerLat, centerLon, precision);
//
//        // Получаем соседние геохеши
//        List<String> neighborGeohashes = Marker.getNeighboringGeohashes(centerGeohash);
//        neighborGeohashes.add(centerGeohash);
//
//        // Создаем список для хранения результатов
//        List<Marker> foundMarkers = new ArrayList<>();
//        List<Task<DataSnapshot>> tasks = new ArrayList<>();
//
//        // Запрашиваем метки для каждого геохеша
//        for (String geohash : neighborGeohashes) {
//            Task<DataSnapshot> task = geohashIndexRef.child(geohash).get()
//                    .addOnSuccessListener(snapshot -> {
//                        for (DataSnapshot markSnapshot : snapshot.getChildren()) {
//                            Marker marker = markSnapshot.getValue(Marker.class);
//                            if (marker != null) {
//                                // Проверяем точное расстояние
//                                if (Marker.calculateDistance(centerLat, centerLon,
//                                        marker.getLatitude(), marker.getLongitude()) <= radiusKm) {
//                                    foundMarkers.add(marker);
//                                }
//                            }
//                        }
//                    });
//            tasks.add(task);
//        }
//
//        // Когда все запросы завершены
//        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
//            if (foundMarkers.isEmpty()) {
//                Toast.makeText(this, "Меток в радиусе " + radiusKm + " км не найдено",
//                        Toast.LENGTH_SHORT).show();
//            } else {
//                openMapWithMarkers(foundMarkers, centerLat, centerLon);
//            }
//        }).addOnFailureListener(e -> {
//            Toast.makeText(this, "Ошибка поиска: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    private void openMapWithMarkers(List<Marker> markers, double centerLat, double centerLon) {
//        Intent intent = new Intent(this, MapMarksActivity.class);
//        intent.putExtra("center_lat", centerLat);
//        intent.putExtra("center_lon", centerLon);
//        intent.putExtra("markers", new ArrayList<>(markers));
//        startActivity(intent);
//    }


    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityMarksBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // Инициализация Firebase
//        mAuth = FirebaseAuth.getInstance();
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//
//        center_latitude2 = findViewById(R.id.center_latitude2);
//        center_height2 = findViewById(R.id.center_height2);
//        marker_text = findViewById(R.id.marker_text);
//        public_marker = findViewById(R.id.public_marker);
//
//        geohashTable = new GeohashTable(7);
//
//        // Находим кнопку по ID
//        Button find_marks2 = findViewById(R.id.find_marks2);
//        // Обработчик клика
//        find_marks2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Создаем Intent для перехода на LoginActivity
//                Intent intent = new Intent(MarksActivity.this, UserMarksActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
////        Button find_marks3 = findViewById(R.id.find_marks3);
////        // Обработчик клика
////        find_marks3.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                // Создаем Intent для перехода на LoginActivity
////                Intent intent = new Intent(MarksActivity.this, HashTableMarksActivity.class);
////                startActivity(intent);
////                finish();
////            }
////        });
//
//    }
//    public void saveMarker(View view) {
//        // Получаем текущего авторизованного пользователя
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String marker_text = binding.markerText.getText().toString().trim();
//        String center_latitude2 = binding.centerLatitude2.getText().toString().trim();
//        String center_height2 = binding.centerHeight2.getText().toString().trim();
//        String center_name = binding.centerName.getText().toString().trim();
//        boolean isPublic = binding.publicMarker.isChecked();
//
//
//
//        // Валидация полей
//        if (marker_text.isEmpty() || center_latitude2.isEmpty() || center_name.isEmpty()) {
//            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            double lat = Double.parseDouble(center_latitude2);
//            double lon = Double.parseDouble(center_height2);
//
//            // Генерируем геохэш (предполагается, что класс GeoHashConverter существует)
//            String geohash = GeoHashConverter.encode(lat, lon, 7); // precision 7 (~150m)
//
//            // Создаем объект метки
//            HashMap<String, Object> marker = new HashMap<>();
//            marker.put("name", center_name);
//            marker.put("latitude", lat);
//            marker.put("longitude", lon);
//            marker.put("geohash", geohash);
//            marker.put("status", isPublic ? "public" : "private");
//            marker.put("userId", currentUser.getUid()); // ID авторизованного пользователя
//            marker.put("note", marker_text);
//            marker.put("timestamp", ServerValue.TIMESTAMP);
//
////          geohashTable.addMarker(lat, lon, new HashMap<>(marker));
//
//            // Получаем уникальный ID для метки
//            String markerId = mDatabase.child("Marks").push().getKey();
//
//            // 1. Сохраняем в основное хранилище Marks
//            mDatabase.child("Marks").child(markerId).setValue(marker)
//                    .addOnCompleteListener(mainTask -> {
//                        if (mainTask.isSuccessful()) {
//                            // 2. Сохраняем в GeohashIndex (без дублирования geohash)
//                            Map<String, Object> indexEntry = new HashMap<>(marker);
//                            indexEntry.remove("geohash");
//
//                            mDatabase.child("GeohashIndex").child(geohash).child(markerId)
//                                    .setValue(indexEntry)
//                                    .addOnCompleteListener(indexTask -> {
//                                        if (indexTask.isSuccessful()) {
//                                            // 3. Локальное хранилище
//                                            geohashTable.addMarker(lat, lon, marker);
//
//                                            runOnUiThread(() -> {
//                                                Toast.makeText(MarksActivity.this,
//                                                        "Метка успешно сохранена",
//                                                        Toast.LENGTH_SHORT).show();
//                                                clearFields();
//                                            });
//                                        } else {
//                                            // Откатываем основное сохранение при ошибке индекса
//                                            mDatabase.child("Marks").child(markerId).removeValue();
//                                            runOnUiThread(() ->
//                                                    Toast.makeText(MarksActivity.this,
//                                                            "Ошибка сохранения индекса: " + indexTask.getException().getMessage(),
//                                                            Toast.LENGTH_LONG).show()
//                                            );
//                                        }
//                                    });
//                        } else {
//                            runOnUiThread(() ->
//                                    Toast.makeText(MarksActivity.this,
//                                            "Ошибка сохранения метки: " + mainTask.getException().getMessage(),
//                                            Toast.LENGTH_LONG).show()
//                            );
//                        }
//                    });
//
//        } catch (NumberFormatException e) {
//            Toast.makeText(this, "Неверный формат координат", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            Toast.makeText(this, "Неизвестная ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void clearFields() {
//        binding.markerText.setText("");
//        binding.centerLatitude2.setText("");
//        binding.centerHeight2.setText("");
//        binding.centerName.setText("");
//        binding.publicMarker.setChecked(false);
//    }
//}

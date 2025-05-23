package com.example.mysportik;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.Locale;






public class MarksActivity extends AppCompatActivity {

    private ActivityMarksBinding binding;
    private EditText center_latitude2, center_height2;
    private DatabaseReference marksRef;
    private DatabaseReference geohashIndexRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        marksRef = FirebaseDatabase.getInstance().getReference("Marks");
        geohashIndexRef = FirebaseDatabase.getInstance().getReference("GeohashIndex");

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
//                @Override
//                public void onSearchComplete(List<Marker> foundMarkers) {
//                    // Переход на UserMarksActivity
//                    Intent intent = new Intent(MarksActivity.this, UserMarksActivity.class);
//                    intent.putParcelableArrayListExtra("found_markers", new ArrayList<>(foundMarkers));
//                    startActivity(intent);
//
//                    // Закрываем диалог
//                    ((AlertDialog) dialog).dismiss();
//                }
//            });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Проверьте правильность ввода данных", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

//    private void searchMarkersInRadius(double centerLat, double centerLon, double radiusKm) {
//        // 1. Генерируем 4-символьный геохеш для центра поиска
//        String targetGeohashPrefix = GeoHashConverter.encode(centerLat, centerLon, 4);
//
//        // 2. Получаем ссылку на корневой узел GeohashIndex
//        DatabaseReference geohashIndexRef = FirebaseDatabase.getInstance()
//                .getReference("GeohashIndex");
//
//        // 3. Поиск всех узлов с совпадающим 4-символьным префиксом
//        geohashIndexRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                List<Marker> publicMarkers = new ArrayList<>();
//
//                // 4. Итерируем по всем узлам GeohashIndex
//                for (DataSnapshot geohashNode : task.getResult().getChildren()) {
//                    String nodeGeohash = geohashNode.getKey();
//
//                    // 5. Проверяем совпадение первых 4 символов
//                    if (nodeGeohash != null && nodeGeohash.startsWith(targetGeohashPrefix)) {
//
//                        // 6. Обрабатываем все метки внутри узла
//                        for (DataSnapshot markerSnapshot : geohashNode.getChildren()) {
//                            Marker marker = markerSnapshot.getValue(Marker.class);
//                            if (marker != null && "public".equals(marker.getStatus())) {
//                                marker.setId(markerSnapshot.getKey());
//                                publicMarkers.add(marker);
//                            }
//                        }
//                    }
//                }
//
//                // 7. Фильтрация по радиусу
//                List<Marker> filteredMarkers = Marker.findMarkersInRadius(
//                        publicMarkers,
//                        centerLat,
//                        centerLon,
//                        radiusKm
//                );
//
//                // 8. Обработка результатов
//                if (filteredMarkers.isEmpty()) {
//                    To
//
//                   ast.makeText(this, "Меток в радиусе " + radiusKm + " км не найдено",
//                            Toast.LENGTH_SHORT).show();
//                } else {
//
//                    openMapWithMarkers(filteredMarkers, centerLat, centerLon);
//                }
//            } else {
//                Toast.makeText(this, "Ошибка загрузки данных: " + task.getException().getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void openMapWithMarkers(List<Marker> markers, double centerLat, double centerLon) {
//        Intent intent = new Intent(this, MapMarksActivity.class);
//        intent.putExtra("center_lat", centerLat);
//        intent.putExtra("center_lon", centerLon);
//        intent.putExtra("markers", new ArrayList<>(markers));
//        startActivity(intent);
//    }




private void openMapWithMarkers(List<Marker> markers, double centerLat, double centerLon) {
    Intent intent = new Intent(this, MapMarksActivity.class);
    intent.putParcelableArrayListExtra("markers_list", new ArrayList<>(markers));
    intent.putExtra("center_lat", centerLat);
    intent.putExtra("center_lon", centerLon);
    startActivity(intent);
}





//
//    private void searchMarkersInRadius(double centerLat, double centerLon, double radiusKm) {
//        // 1. Генерируем 4-символьный геохеш
//        String targetGeohashPrefix = GeoHashConverter.encode(centerLat, centerLon, 4);
//
//        // 2. Получаем ссылку на GeohashIndex
//        DatabaseReference geohashIndexRef = FirebaseDatabase.getInstance()
//                .getReference("GeohashIndex");
//
//        // Показываем индикатор загрузки
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Идёт поиск меток...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        // 3. Поиск в Firebase
//        geohashIndexRef.get().addOnCompleteListener(task -> {
//            progressDialog.dismiss(); // Скрываем индикатор
//
//            if (task.isSuccessful()) {
//                List<Marker> publicMarkers = new ArrayList<>();
//
//                // Обработка результатов
//                for (DataSnapshot geohashNode : task.getResult().getChildren()) {
//                    String nodeGeohash = geohashNode.getKey();
//                    if (nodeGeohash != null && nodeGeohash.startsWith(targetGeohashPrefix)) {
//                        for (DataSnapshot markerSnapshot : geohashNode.getChildren()) {
//                            Marker marker = markerSnapshot.getValue(Marker.class);
//                            if (marker != null && "public".equals(marker.getStatus())) {
//                                marker.setId(markerSnapshot.getKey());
//                                publicMarkers.add(marker);
//                            }
//                        }
//                    }
//                }
//
//                // Фильтрация по радиусу
//                List<Marker> filteredMarkers = Marker.findMarkersInRadius(
//                        publicMarkers, centerLat, centerLon, radiusKm);
//
//                // Показываем результаты в диалоге
//                showResultsDialog(filteredMarkers, centerLat, centerLon, radiusKm);
//
//            } else {
//                Toast.makeText(this, "Ошибка загрузки: " + task.getException().getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void showResultsDialog(List<Marker> markers, double centerLat, double centerLon, double radius) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Результаты поиска");
//
//        // Формируем сообщение
//        String message = String.format(Locale.getDefault(),
//                "Найдено меток: %d\nРадиус поиска: %.1f км\nЦентр: %.6f, %.6f",
//                markers.size(), radius, centerLat, centerLon);
//
//        if (!markers.isEmpty()) {
//            message += "\n\nПервые 5 меток:";
//            int count = Math.min(markers.size(), 5);
//            for (int i = 0; i < count; i++) {
//                Marker m = markers.get(i);
//                message += String.format(Locale.getDefault(),
//                        "\n%d. %s (%.6f, %.6f)",
//                        i+1, m.getName(), m.getLatitude(), m.getLongitude());
//            }
//        }
//
//        builder.setMessage(message);
//
//        // Кнопки диалога
//        builder.setPositiveButton("Показать на карте", (dialog, which) -> {
//            openMapWithMarkers(markers, centerLat, centerLon);
//        });
//
//        builder.setNegativeButton("Закрыть", null);
//
//        // Дополнительная кнопка для экспорта
//        builder.setNeutralButton("Экспорт в CSV", (dialog, which) -> {
//            exportToCsv(markers);
//        });
//
//        builder.show();
//    }

//    private void exportToCsv(List<Marker> markers) {
//        // Реализация экспорта (пример)
//        StringBuilder csv = new StringBuilder("ID,Name,Latitude,Longitude\n");
//        for (Marker m : markers) {
//            csv.append(String.format(Locale.US, "%s,%s,%.6f,%.6f\n",
//                    m.getId(), m.getName(), m.getLatitude(), m.getLongitude()));
//        }
//
//        // Здесь должна быть логика сохранения файла
//        Toast.makeText(this, "Готово к экспорту " + markers.size() + " меток",
//                Toast.LENGTH_SHORT).show();
//    }
//

    private void searchMarkersInRadius(double centerLat, double centerLon, double radiusKm) {
        // Показываем индикатор загрузки
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Идет поиск меток...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String targetGeohashPrefix = GeoHashConverter.encode(centerLat, centerLon, 4);
        DatabaseReference geohashIndexRef = FirebaseDatabase.getInstance()
                .getReference("GeohashIndex");

        geohashIndexRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                List<Marker> publicMarkers = new ArrayList<>();

                for (DataSnapshot geohashNode : task.getResult().getChildren()) {
                    String nodeGeohash = geohashNode.getKey();
                    if (nodeGeohash != null && nodeGeohash.startsWith(targetGeohashPrefix)) {
                        for (DataSnapshot markerSnapshot : geohashNode.getChildren()) {
                            Marker marker = markerSnapshot.getValue(Marker.class);
                            if (marker != null && "public".equals(marker.getStatus())) {
                                marker.setId(markerSnapshot.getKey());
                                publicMarkers.add(marker);
                            }
                        }
                    }
                }

                List<Marker> filteredMarkers = Marker.findMarkersInRadius(
                        publicMarkers, centerLat, centerLon, radiusKm);

                if (filteredMarkers.isEmpty()) {
                    Toast.makeText(this, "Меток не найдено", Toast.LENGTH_SHORT).show();
                } else {
                    showResultsDialog(filteredMarkers, centerLat, centerLon, radiusKm);
                }
            } else {
                Toast.makeText(this, "Ошибка поиска: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showResultsDialog(List<Marker> markers, double centerLat, double centerLon, double radius) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Результаты поиска");

        // Загружаем кастомный layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_markers_results, null);
        TextView summaryText = dialogView.findViewById(R.id.summary_text);
        TextView markersText = dialogView.findViewById(R.id.markers_text);

        // Устанавливаем сводную информацию
        String summary = String.format(Locale.getDefault(),
                "Найдено меток: %d\nРадиус: %.1f км\nЦентр: %.6f, %.6f",
                markers.size(), radius, centerLat, centerLon);
        summaryText.setText(summary);

        // Формируем текст со всеми метками
        StringBuilder markersContent = new StringBuilder();
        for (int i = 0; i < markers.size(); i++) {
            Marker marker = markers.get(i);
            markersContent.append(String.format(Locale.getDefault(),
                    "%d. %s\nШирота: %.6f\nДолгота: %.6f\n\n",
                    i + 1,
                    marker.getName(),
                    marker.getLatitude(),
                    marker.getLongitude()));
        }

        markersText.setText(markersContent.toString());
        markersText.setMovementMethod(new ScrollingMovementMethod()); // Включаем прокрутку

        builder.setView(dialogView);

        builder.setPositiveButton("Показать на карте", (dialog, which) -> {
            openMapWithMarkers(markers, centerLat, centerLon);
        });

        builder.setNegativeButton("Закрыть", null);

        // Создаем и показываем диалог
        AlertDialog dialog = builder.create();
        dialog.show();
    }










    public void saveMarker(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не зарегистрирован", Toast.LENGTH_SHORT).show();
            return;
        }

        String marker_text = binding.markerText.getText().toString().trim();
        String center_latitude2 = binding.centerLatitude2.getText().toString().trim();
        String center_height2 = binding.centerHeight2.getText().toString().trim();
        String center_name = binding.centerName.getText().toString().trim();
        boolean isPublic = binding.publicMarker.isChecked();

        if (marker_text.isEmpty() || center_latitude2.isEmpty() || center_name.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
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
                                            //geohashTable.addMarker(lat, lon, marker);
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
}

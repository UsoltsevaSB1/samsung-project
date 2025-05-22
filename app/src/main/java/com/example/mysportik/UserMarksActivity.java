package com.example.mysportik;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserMarksActivity extends AppCompatActivity {

    private ListView publicMarksListView;
    private ListView privateMarksListView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private List<Marker> publicMarkers = new ArrayList<>();
    private List<Marker> privateMarkers = new ArrayList<>();

    private ImageButton searchButton;
    private EditText radiusEditText;
    private EditText selectedMarkerEditText;
    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_marks);

        // Инициализация Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Marks");

        // Инициализация UI элементов
        publicMarksListView = findViewById(R.id.publicMarksList);
        privateMarksListView = findViewById(R.id.privateMarksList);
        searchButton = findViewById(R.id.searchButton);
        radiusEditText = findViewById(R.id.radiusEditText);
        selectedMarkerEditText = findViewById(R.id.selectedMarkerTextView);

        // Загрузка меток пользователя
        loadUserMarks();

        // Установка слушателей кликов
        setupListClickListeners();

        // Обработчик кнопки "Назад"
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserMarksActivity.this, MarksActivity.class);
            startActivity(intent);
            finish();
        });

//        // Обработчик кнопки поиска
//        searchButton.setOnClickListener(v -> {
//            if (selectedMarker == null) {
//                Toast.makeText(this, "Сначала выберите метку", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String radiusText = radiusEditText.getText().toString();
//            if (radiusText.isEmpty()) {
//                Toast.makeText(this, "Введите радиус поиска", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            try {
//                double radius = Double.parseDouble(radiusText);
//                searchMarkersInRadius(selectedMarker.getLatitude(),
//                        selectedMarker.getLongitude(),
//                        radius);
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Введите корректное число", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

//    private void searchMarkersInRadius(double centerLat, double centerLon, double radiusKm) {
//        List<Marker> allMarkers = new ArrayList<>();
//        allMarkers.addAll(publicMarkers);
//        allMarkers.addAll(privateMarkers);
//
//        List<Marker> markersInRadius = Marker.findMarkersInRadiusExact(
//                allMarkers, centerLat, centerLon, radiusKm);
//
//        if (markersInRadius.isEmpty()) {
//            Toast.makeText(this, "В радиусе " + radiusKm + " км маркеров не найдено",
//                    Toast.LENGTH_SHORT).show();
//        } else {
//            String message = "Найдено маркеров: " + markersInRadius.size();
//            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//            // Открываем карту с найденными маркерами
//            openMapWithMarkers(markersInRadius);
//        }
//    }

//    private void openMapWithMarkers(List<Marker> markers) {
//        Intent mapIntent = new Intent(this, MapMarksActivity.class);
//        mapIntent.putExtra("markers", new ArrayList<>(markers));
//        startActivity(mapIntent);
//    }

    private void setupListClickListeners() {
        publicMarksListView.setOnItemClickListener((parent, view, position, id) -> {
            // Получаем выбранную метку из публичного списка
            selectedMarker = publicMarkers.get(position);
            // Устанавливаем название метки в TextView
            selectedMarkerEditText.setText(selectedMarker.getName());
            // Устанавливаем зеленый цвет для публичных меток
            selectedMarkerEditText.setTextColor(Color.GREEN);
            // Очищаем поле радиуса для нового поиска
            radiusEditText.setText("");
        });

        privateMarksListView.setOnItemClickListener((parent, view, position, id) -> {
            // Получаем выбранную метку из приватного списка
            selectedMarker = privateMarkers.get(position);
            // Устанавливаем название метки в TextView
            selectedMarkerEditText.setText(selectedMarker.getName());
            // Устанавливаем красный цвет для приватных меток
            selectedMarkerEditText.setTextColor(Color.RED);
            // Очищаем поле радиуса для нового поиска
            radiusEditText.setText("");
        });
    }

    private void loadUserMarks() {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        databaseReference.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        publicMarkers.clear();
                        privateMarkers.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Marker marker = snapshot.getValue(Marker.class);
                            if (marker != null) {
                                if ("public".equals(marker.getStatus())) {
                                    publicMarkers.add(marker);
                                } else {
                                    privateMarkers.add(marker);
                                }
                            }
                        }

                        updateLists();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserMarksActivity.this,
                                "Ошибка загрузки меток: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLists() {
        ArrayAdapter<Marker> publicAdapter = new ArrayAdapter<Marker>(
                this, android.R.layout.simple_list_item_1, publicMarkers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.GREEN);
                textView.setText(getItem(position).getName());
                return view;
            }
        };

        ArrayAdapter<Marker> privateAdapter = new ArrayAdapter<Marker>(
                this, android.R.layout.simple_list_item_1, privateMarkers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.RED);
                textView.setText(getItem(position).getName());
                return view;
            }
        };

        publicMarksListView.setAdapter(publicAdapter);
        privateMarksListView.setAdapter(privateAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserMarks();
    }
}
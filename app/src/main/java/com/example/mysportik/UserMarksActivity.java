package com.example.mysportik;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import android.view.ViewGroup;

public class UserMarksActivity extends AppCompatActivity {

    private ListView publicMarksListView;
    //private ListView privateMarksListView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private List<Marker> publicMarkers = new ArrayList<>();
    //private List<Marker> privateMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_marks);

        // Инициализация Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Marks");

        // Инициализация UI элементов
        publicMarksListView = findViewById(R.id.publicMarksList);


        // Загрузка меток пользователя
        loadUserMarks();

        // Находим кнопку по ID
        ImageButton backButton = findViewById(R.id.backButton);
        // Обработчик клика
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем Intent для перехода на LoginActivity
                Intent intent = new Intent(UserMarksActivity.this, MarksActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadUserMarks() {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        databaseReference.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        publicMarkers.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Marker marker = snapshot.getValue(Marker.class);
                            if (marker != null) {
                                publicMarkers.add(marker);
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
        // Адаптер для публичных меток (зеленый цвет)
        ArrayAdapter<Marker> publicAdapter = new ArrayAdapter<Marker>(
                this,
                android.R.layout.simple_list_item_1,
                publicMarkers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.GREEN);
                textView.setText(getItem(position).getName());
                return view;
            }
        };
        publicMarksListView.setAdapter(publicAdapter);
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные при возвращении на экран
        loadUserMarks();
    }
}
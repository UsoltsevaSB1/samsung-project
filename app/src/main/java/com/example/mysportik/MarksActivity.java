package com.example.mysportik;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mysportik.databinding.ActivityMarksBinding;
import com.example.mysportik.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.List;

public class MarksActivity extends AppCompatActivity {

    private GeohashStorage storage;
    EditText center_height2, center_latitude2, marker_text;
    RadioButton public_marker;


    private ActivityMarksBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    //private DatabaseReference markersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        center_latitude2 = findViewById(R.id.center_latitude2);
        center_height2 = findViewById(R.id.center_height2);
        marker_text = findViewById(R.id.marker_text);
        public_marker = findViewById(R.id.public_marker);





    }
    public void saveMarker(View view) {
        // Получаем текущего авторизованного пользователя
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String marker_text = binding.markerText.getText().toString().trim();
        String center_latitude2 = binding.centerLatitude2.getText().toString().trim();
        String center_height2 = binding.centerHeight2.getText().toString().trim();
        boolean isPublic = binding.publicMarker.isChecked();



        // Валидация полей
        if (marker_text.isEmpty() || center_latitude2.isEmpty() || center_height2.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double lat = Double.parseDouble(center_latitude2);
            double lon = Double.parseDouble(center_height2);

            // Генерируем геохэш (предполагается, что класс GeoHashConverter существует)
            String geohash = GeoHashConverter.encode(lat, lon, 7); // precision 7 (~150m)

            // Создаем объект метки
            HashMap<String, Object> marker = new HashMap<>();
            marker.put("latitude", lat);
            marker.put("longitude", lon);
            marker.put("geohash", geohash);
            marker.put("status", isPublic ? "public" : "private");
            marker.put("userId", currentUser.getUid()); // ID авторизованного пользователя
            marker.put("note", marker_text);
            marker.put("timestamp", ServerValue.TIMESTAMP);

            // Сохраняем в корень "Marks"
            mDatabase.child("Marks").push().setValue(marker)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MarksActivity.this,
                                        "Marker saved successfully",
                                        Toast.LENGTH_SHORT).show();
                                clearFields();
                            } else {
                                Toast.makeText(MarksActivity.this,
                                        "Failed to save marker: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid coordinates format", Toast.LENGTH_SHORT).show();
        }
    }
    private void clearFields() {
        binding.markerText.setText("");
        binding.centerLatitude2.setText("");
        binding.centerHeight2.setText("");
        binding.publicMarker.setChecked(false);
    }
}

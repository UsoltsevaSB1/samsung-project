package com.example.mysportik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent( MainActivity.this, LoginActivity.class));
        }

        Button marks = findViewById(R.id.to_marks);
        marks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MarksActivity.class));
                finish();
            }
        });
        runGeoHashTests();

    }

    private void runGeoHashTests() {
        // Запускаем в отдельном потоке, чтобы не блокировать UI
        new Thread(() -> {
            Log.d("GeoHashTest", "=== Начало тестирования ===");
            GeoHashTester.runAllTests();
            Log.d("GeoHashTest", "=== Тестирование завершено ===");

            // Если нужно обновить UI после тестов
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this,
                        "Тесты завершены, смотрите логи",
                        Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
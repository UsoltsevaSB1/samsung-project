package com.example.mysportik;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;

public class UserActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    //private CircleImageView avatarImageView;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView changeAvatarText;
    private Button backButton;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    //private StorageReference mStorage;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //mStorage = FirebaseStorage.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        // Инициализация UI
        //avatarImageView = findViewById(R.id.avatarImageView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        changeAvatarText = findViewById(R.id.changeAvatarText);
        backButton = findViewById(R.id.backButton);

        // Загрузка данных пользователя
        loadUserData();

        // Обработчики кликов
        changeAvatarText.setOnClickListener(v -> openImageChooser());
        //avatarImageView.setOnClickListener(v -> openImageChooser());
        backButton.setOnClickListener(v -> returnToMap());
    }

    private void loadUserData() {
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    userNameTextView.setText(username != null ? username : "Без имени");
                    userEmailTextView.setText(email != null ? email : "Email не указан");

                    // Загрузка аватарки
                    if (snapshot.hasChild("avatarUrl")) {
                        String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                        //loadUserAvatar(avatarUrl);
                    }
                } else {
                    Toast.makeText(UserActivity.this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, "Ошибка загрузки данных: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void loadUserAvatar(String avatarUrl) {
//        if (avatarUrl != null && !avatarUrl.isEmpty()) {
//            Glide.with(this)
//                    .load(avatarUrl)
//                    .placeholder(R.drawable.avatar)
//                    .error(R.drawable.avatar)
//                    .into(avatarImageView);
//        }
//    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGE_REQUEST);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Uri imageUri = data.getData();
//
//            // Показать выбранное изображение
//            Glide.with(this)
//                    .load(imageUri)
//                    .placeholder(R.drawable.avatar)
//                    .into(avatarImageView);
//
//            // Загрузить изображение в Firebase Storage
//            uploadImageToFirebase(imageUri);
//        }
//    }

//    private void uploadImageToFirebase(Uri imageUri) {
//        // Создание ссылки в Storage
//        StorageReference avatarRef = mStorage.child("avatars/" + userId + ".jpg");
//
//        // Загрузка файла
//        avatarRef.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> {
//                    // Получение URL загруженного изображения
//                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        // Сохранение URL в базе данных
//                        saveAvatarUrlToDatabase(uri.toString());
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    private void saveAvatarUrlToDatabase(String imageUrl) {
        // Обновление данных пользователя
        mDatabase.child("Users").child(userId).child("avatarUrl").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Фото профиля обновлено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void returnToMap() {
        startActivity(new Intent(this, MapMarksActivity.class));
        finish();
    }
}
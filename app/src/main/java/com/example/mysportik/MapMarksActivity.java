package com.example.mysportik;

import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    private Button btnAddMarker;
    private boolean isMarkerMode = false;
    private PlacemarkMapObject tempMarker;
    private boolean isProcessingMarkerClick = false;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private HashMap<PlacemarkMapObject, String> markerIdMap = new HashMap<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    // Переменная класса для хранения диалога
    private AlertDialog createNoteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        MapKitFactory.setApiKey("4f7c3577-6bf8-4a85-9d77-552f8d759852");
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_map_marks);

        mapView = findViewById(R.id.mapview);
        btnAddMarker = findViewById(R.id.btn_add_marker);

        mapView.getMap().move(new CameraPosition(new Point(51.7373, 36.1874), 10, 0, 0));

        ImageButton backButton = findViewById(R.id.backButton2);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(MapMarksActivity.this, LoginActivity.class));
            finish();
        });

        btnAddMarker.setOnClickListener(v -> {
            isMarkerMode = !isMarkerMode;
            updateButtonState();
            updateMapListeners();
        });

        loadExistingMarkers();

        findViewById(R.id.btn_account).setOnClickListener(v -> {
            startActivity(new Intent(MapMarksActivity.this, UserActivity.class));
            finish();
        });

        findViewById(R.id.btn_search).setOnClickListener(v -> {
            // Переход в поиск (реализуйте по необходимости)
        });
    }

    private void updateButtonState() {
        btnAddMarker.setBackgroundColor(isMarkerMode ? 0xFF4CAF50 : 0xFFFFFFFF);
        btnAddMarker.setText(isMarkerMode ? "Отменить" : "Добавить метку");
    }

    private InputListener mapInputListener;
    private MapObjectTapListener markerTapListener;

    private void updateMapListeners() {
        if (mapInputListener != null) {
            mapView.getMap().removeInputListener(mapInputListener);
        }
        if (markerTapListener != null) {
            mapView.getMap().getMapObjects().removeTapListener(markerTapListener);
        }

        if (isMarkerMode) {
            mapInputListener = new InputListener() {
                @Override
                public void onMapTap(Map map, Point point) {
                    if (tempMarker != null) {
                        mapView.getMap().getMapObjects().remove(tempMarker);
                    }
                    tempMarker = addMarker(point, true);
                    showCreateNoteDialog(point, tempMarker);
                }

                @Override
                public void onMapLongTap(Map map, Point point) {}
            };
            mapView.getMap().addInputListener(mapInputListener);
        } else {
            markerTapListener = (mapObject, point) -> {
                if (mapObject instanceof PlacemarkMapObject && !isProcessingMarkerClick) {
                    isProcessingMarkerClick = true;
                    handleMarkerClick((PlacemarkMapObject) mapObject);
                    return true;
                }
                return false;
            };
            mapView.getMap().getMapObjects().addTapListener(markerTapListener);
        }
    }

    private void handleMarkerClick(PlacemarkMapObject marker) {
        animateMarker(marker, () -> {
            showExistingNoteDetails(marker);
            new Handler(Looper.getMainLooper()).postDelayed(() -> isProcessingMarkerClick = false, 500);
        });
    }

    private void animateMarker(PlacemarkMapObject marker, Runnable onAnimationEnd) {
        IconStyle iconStyleSmall = new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(0.2f);
        IconStyle iconStyleLarge = new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(0.26f);

        marker.setIconStyle(iconStyleLarge);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            marker.setIconStyle(iconStyleSmall);
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
        }, 150);
    }

    private PlacemarkMapObject addMarker(Point point, boolean isTemporary) {
        PlacemarkMapObject marker = mapView.getMap().getMapObjects().addPlacemark(point);
        IconStyle iconStyle = new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(0.2f);
        int markerIconRes = R.drawable.ic_map_marker;
        marker.setIcon(ImageProvider.fromResource(this, markerIconRes), iconStyle);

        if (!isTemporary) {
            marker.addTapListener((mapObject, pt) -> {
                handleMarkerClick((PlacemarkMapObject) mapObject);
                return true;
            });
        }
        return marker;
    }

    private void showCreateNoteDialog(Point point, PlacemarkMapObject marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_note_name);
        EditText etNote = dialogView.findViewById(R.id.et_note_text);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_note_status);
        ImageButton btnAddPhoto = dialogView.findViewById(R.id.btn_add_photo);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_photo_preview);

        selectedImageUri = null;
        ivPreview.setVisibility(View.GONE);

        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGE_REQUEST);
        });

        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", (d, which) -> {
            String name = etName.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            boolean hasError = false;

            if (name.isEmpty()) {
                etName.setError("Введите название");
                hasError = true;
            }
            if (note.isEmpty()) {
                etNote.setError("Введите заметку");
                hasError = true;
            }

            if (!hasError) {
                String status = rgStatus.getCheckedRadioButtonId() == R.id.rb_public ? "public" : "private";
                if (selectedImageUri != null) {
                    uploadPhotoAndSaveNote(point, name, note, status, marker);
                } else {
                    saveNoteToFirebase(point, name, note, status, null, marker);
                }
            } else {
                dialog.dismiss();
                dialog.show();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", (d, which) -> {
            if (marker != null) {
                mapView.getMap().getMapObjects().remove(marker);
                tempMarker = null;
            }
        });

        dialog.show();
    }
    //обработка результата выбора фотки
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            //AlertDialog dialog = (AlertDialog) LayoutInflater.from(this).getLayoutInflater().inflate(R.layout.dialog_create_note, null).getTag();
            if (createNoteDialog != null && createNoteDialog.isShowing()) {
                ImageView ivPreview = createNoteDialog.findViewById(R.id.iv_photo_preview);
                ivPreview.setImageURI(selectedImageUri);
                ivPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private void uploadPhotoAndSaveNote(Point point, String name, String note, String status, PlacemarkMapObject marker) {
        StorageReference photoRef = mStorage.child("marker_photos/" + System.currentTimeMillis() + ".jpg");
        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveNoteToFirebase(point, name, note, status, uri.toString(), marker);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки фото: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveNoteToFirebase(point, name, note, status, null, marker);
                });
    }

    private void showExistingNoteDetails(PlacemarkMapObject mapObject) {
        String markerId = markerIdMap.get(mapObject);
        if (markerId == null) return;

        mDatabase.child("Marks").child(markerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Marker marker = snapshot.getValue(Marker.class);
                if (marker != null) {
                    showNoteDetailsDialog(marker, mapObject);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapMarksActivity.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNoteDetailsDialog(Marker marker, PlacemarkMapObject mapObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note_details, null);

        TextView tvName = dialogView.findViewById(R.id.tv_note_name);
        TextView tvNote = dialogView.findViewById(R.id.tv_note_text);
        TextView tvStatus = dialogView.findViewById(R.id.tv_note_status);
        ImageButton btnOptions = dialogView.findViewById(R.id.btn_options);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close);
        ImageView ivPhoto = dialogView.findViewById(R.id.iv_note_photo);

        tvName.setText(marker.getName());
        tvNote.setText(marker.getNote());
        tvStatus.setText(marker.getStatus().equals("public") ? "Публичная" : "Приватная");

        if (marker.getPhotoUrl() != null) {
            Glide.with(this).load(marker.getPhotoUrl()).into(ivPhoto);
            ivPhoto.setVisibility(View.VISIBLE);
        } else {
            ivPhoto.setVisibility(View.GONE);
        }

        AlertDialog dialog = builder.setView(dialogView).create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnOptions.setOnClickListener(v -> {
            showNoteOptionsMenu(v, marker, mapObject, dialog);
        });

        dialog.show();
    }

    private void showNoteOptionsMenu(View anchor, Marker marker, PlacemarkMapObject mapObject, AlertDialog parentDialog) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.note_options_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_edit) {
                showEditNoteDialog(marker, mapObject);
                parentDialog.dismiss();
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                deleteMarker(marker, mapObject);
                parentDialog.dismiss();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showEditNoteDialog(Marker marker, PlacemarkMapObject mapObject) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_note_name);
        EditText etNote = dialogView.findViewById(R.id.et_note_text);
        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_note_status);
        ImageButton btnAddPhoto = dialogView.findViewById(R.id.btn_add_photo);
        ImageView ivPreview = dialogView.findViewById(R.id.iv_photo_preview);

        etName.setText(marker.getName());
        etNote.setText(marker.getNote());
        rgStatus.check(marker.getStatus().equals("public") ? R.id.rb_public : R.id.rb_private);

        selectedImageUri = null;
        if (marker.getPhotoUrl() != null) {
            Glide.with(this).load(marker.getPhotoUrl()).into(ivPreview);
            ivPreview.setVisibility(View.VISIBLE);
        } else {
            ivPreview.setVisibility(View.GONE);
        }

        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Выберите фото"), PICK_IMAGE_REQUEST);
        });

        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", (d, which) -> {
            String name = etName.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            boolean hasError = false;

            if (name.isEmpty()) {
                etName.setError("Введите название");
                hasError = true;
            }
            if (note.isEmpty()) {
                etNote.setError("Введите заметку");
                hasError = true;
            }

            if (!hasError) {
                marker.setName(name);
                marker.setNote(note);
                marker.setStatus(rgStatus.getCheckedRadioButtonId() == R.id.rb_public ? "public" : "private");
                if (selectedImageUri != null) {
                    uploadPhotoAndUpdateMarker(marker, mapObject);
                } else {
                    updateMarkerInFirebase(marker, mapObject);
                }
            } else {
                dialog.dismiss();
                dialog.show();
            }
        });

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", (d, which) -> {});
        dialog.show();
    }

    private void uploadPhotoAndUpdateMarker(Marker marker, PlacemarkMapObject mapObject) {
        StorageReference photoRef = mStorage.child("marker_photos/" + marker.getId() + ".jpg");
        photoRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    marker.setPhotoUrl(uri.toString());
                    updateMarkerInFirebase(marker, mapObject);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки фото: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateMarkerInFirebase(marker, mapObject);
                });
    }

    private void updateMarkerInFirebase(Marker marker, PlacemarkMapObject mapObject) {
        mDatabase.child("Marks").child(marker.getId()).setValue(marker)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Метка обновлена", Toast.LENGTH_SHORT).show();
                    int markerIconRes = marker.getStatus().equals("public")
                            ? R.drawable.ic_map_marker
                            : R.drawable.ic_map_marker_pr;
                    mapObject.setIcon(
                            ImageProvider.fromResource(this, markerIconRes),
                            new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(0.2f)
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteMarker(Marker marker, PlacemarkMapObject mapObject) {
        mDatabase.child("Marks").child(marker.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    mapView.getMap().getMapObjects().remove(mapObject);
                    markerIdMap.remove(mapObject);
                    Toast.makeText(this, "Метка удалена", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveNoteToFirebase(Point point, String name, String note, String status, String photoUrl, PlacemarkMapObject marker) {
        String key = mDatabase.child("Marks").push().getKey();
        if (key == null) return;

        Marker firebaseMarker = new Marker();
        firebaseMarker.setId(key);
        firebaseMarker.setLatitude(point.getLatitude());
        firebaseMarker.setLongitude(point.getLongitude());
        firebaseMarker.setName(name);
        firebaseMarker.setNote(note);
        firebaseMarker.setStatus(status);
        firebaseMarker.setTimestamp(System.currentTimeMillis());
        firebaseMarker.setPhotoUrl(photoUrl);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firebaseMarker.setUserId(user.getUid());
        }

        mDatabase.child("Marks").child(key).setValue(firebaseMarker)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
                    markerIdMap.put(marker, key);
                    int markerIconRes = status.equals("public")
                            ? R.drawable.ic_map_marker
                            : R.drawable.ic_map_marker_pr;
                    marker.setIcon(
                            ImageProvider.fromResource(this, markerIconRes),
                            new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(0.2f)
                    );
                    marker.addTapListener((mapObject, pt) -> {
                        handleMarkerClick((PlacemarkMapObject) mapObject);
                        return true;
                    });
                })
                .addOnFailureListener(e -> {
                    mapView.getMap().getMapObjects().remove(marker);
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadExistingMarkers() {
        mDatabase.child("Marks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot markSnapshot : snapshot.getChildren()) {
                    Marker marker = markSnapshot.getValue(Marker.class);
                    if (marker != null) {
                        Point point = new Point(marker.getLatitude(), marker.getLongitude());
                        PlacemarkMapObject mapObject = addMarker(point, false);
                        String status = marker.getStatus();
                        int markerIconRes = (status != null && status.equals("public"))
                                ? R.drawable.ic_map_marker
                                : R.drawable.ic_map_marker_pr;
                        mapObject.setIcon(
                                ImageProvider.fromResource(MapMarksActivity.this, markerIconRes),
                                new IconStyle().setAnchor(new PointF(0.5f, 1.0f)).setScale(0.2f)
                        );
                        markerIdMap.put(mapObject, marker.getId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapMarksActivity.this, "Ошибка загрузки меток: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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


//package com.example.mysportik;
//
//import android.content.Intent;
//import android.graphics.PointF;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.ScaleAnimation;
//import android.widget.Button;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.ImageButton;
//import android.widget.EditText;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.yandex.mapkit.MapKitFactory;
//import com.yandex.mapkit.geometry.Point;
//import com.yandex.mapkit.map.CameraPosition;
//import com.yandex.mapkit.map.IconStyle;
//import com.yandex.mapkit.map.PlacemarkMapObject;
//import com.yandex.runtime.image.ImageProvider;
//import com.yandex.mapkit.map.InputListener;
//import com.yandex.mapkit.map.Map;
//import com.yandex.mapkit.map.MapObject;
//import com.yandex.mapkit.map.MapObjectTapListener;
//import com.yandex.mapkit.mapview.MapView;
//
//import java.util.HashMap;
//
//public class MapMarksActivity extends AppCompatActivity {
//    private MapView mapView;
//    private Button btnAddMarker;
//    private boolean isMarkerMode = false;
//    private PlacemarkMapObject tempMarker;
//    private boolean isProcessingMarkerClick = false; // Флаг для предотвращения множественных кликов
//
//    private DatabaseReference mDatabase;
//    private FirebaseAuth mAuth;
//    private HashMap<PlacemarkMapObject, String> markerIdMap = new HashMap<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mAuth = FirebaseAuth.getInstance();
//
//        MapKitFactory.setApiKey("4f7c3577-6bf8-4a85-9d77-552f8d759852");
//        MapKitFactory.initialize(this);
//        setContentView(R.layout.activity_map_marks);
//
//        mapView = findViewById(R.id.mapview);
//        btnAddMarker = findViewById(R.id.btn_add_marker);
//
//        // Установка начальной позиции карты
//        mapView.getMap().move(new CameraPosition(new Point(51.7373, 36.1874), 10, 0, 0));
//
//        ImageButton backButton;
//        backButton = findViewById(R.id.backButton2);
//        // Обработчик клика
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Создаем Intent для перехода на LoginActivity
//                Intent intent = new Intent(MapMarksActivity.this, LoginActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
//
//        // Обработчики кнопок
//        btnAddMarker.setOnClickListener(v -> {
//            isMarkerMode = !isMarkerMode;
//            updateButtonState();
//            updateMapListeners();
//        });
//
//        // Загрузка существующих меток
//        loadExistingMarkers();
//
//        Button marks = findViewById(R.id.btn_account);
//        marks.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MapMarksActivity.this, UserActivity.class));
//                finish();
//            }
//        });
//
////        findViewById(R.id.btn_account).setOnClickListener(v -> {
////            // Переход в аккаунт
////        });
//
//        findViewById(R.id.btn_search).setOnClickListener(v -> {
//            // Переход в поиск
//        });
//    }
//
//    private void updateButtonState() {
//        btnAddMarker.setBackgroundColor(isMarkerMode ? 0xFF4CAF50 : 0xFFFFFFFF);
////        btnAddMarker.setText(isMarkerMode ? R.string.cancel_marker_mode : R.string.add_marker);
//        btnAddMarker.setText(isMarkerMode ? "Отменить" : "Добавить метку");
//    }
//
//    private InputListener mapInputListener;
//    private MapObjectTapListener markerTapListener;
//
//    private void updateMapListeners() {
//        if (mapInputListener != null) {
//            mapView.getMap().removeInputListener(mapInputListener);
//        }
//        if (markerTapListener != null) {
//            mapView.getMap().getMapObjects().removeTapListener(markerTapListener);
//        }
//
//        if (isMarkerMode) {
//            // В режиме добавления метки
//            mapInputListener = new InputListener() {
//                @Override
//                public void onMapTap(Map map, Point point) {
//                    if (tempMarker != null) {
//                        mapView.getMap().getMapObjects().remove(tempMarker);
//                    }
//                    tempMarker = addMarker(point, true);
//                    showCreateNoteDialog(point, tempMarker);
//                }
//
//                @Override
//                public void onMapLongTap(Map map, Point point) {}
//            };
//            mapView.getMap().addInputListener(mapInputListener);
//        } else {
//            // В обычном режиме - слушаем клики по меткам
//            markerTapListener = (mapObject, point) -> {
//                if (mapObject instanceof PlacemarkMapObject) {
//                    handleMarkerClick((PlacemarkMapObject) mapObject);
//                    return true;
//                }
//                return false;
//            };
//            mapView.getMap().getMapObjects().addTapListener(markerTapListener);
//        }
//    }
//
//    private void handleMarkerClick(PlacemarkMapObject marker) {
//        if (isProcessingMarkerClick) return;
//        isProcessingMarkerClick = true;
//
//        // Анимация при нажатии на метку
//        animateMarker(marker, () -> {
//            showExistingNoteDetails(marker);
//            isProcessingMarkerClick = false;
//        });
//    }
//
//    private void animateMarker(PlacemarkMapObject marker, Runnable onAnimationEnd) {
//        // Создаем анимацию масштабирования
//        ScaleAnimation scaleAnimation = new ScaleAnimation(
//                1f, 1.3f, // Начальный и конечный масштаб по X
//                1f, 1.3f, // Начальный и конечный масштаб по Y
//                Animation.RELATIVE_TO_SELF, 0.5f, // Точка трансформации по X
//                Animation.RELATIVE_TO_SELF, 1.0f // Точка трансформации по Y (нижняя часть)
//        );
//
//        scaleAnimation.setDuration(150);
//        scaleAnimation.setRepeatCount(1);
//        scaleAnimation.setRepeatMode(Animation.REVERSE);
//        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {}
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                if (onAnimationEnd != null) {
//                    onAnimationEnd.run();
//                }
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {}
//        });
//
//        // Применяем анимацию к View (не к иконке напрямую)
//        // Для этого нужно получить View маркера, но в Yandex MapKit это сложно
//
//        // Альтернативное решение - создать две иконки разного размера и переключать их
//        IconStyle iconStyleSmall = new IconStyle()
//                .setAnchor(new PointF(0.5f, 1.0f))
//                .setScale(0.2f);
//
//        IconStyle iconStyleLarge = new IconStyle()
//                .setAnchor(new PointF(0.5f, 1.0f))
//                .setScale(0.26f); // 30% больше
//
//        // Быстро переключаем иконки для эффекта анимации
//        marker.setIconStyle(iconStyleLarge);
//        new Handler().postDelayed(() -> {
//            marker.setIconStyle(iconStyleSmall);
//            if (onAnimationEnd != null) {
//                onAnimationEnd.run();
//            }
//        }, 150);
//    }
//
//    private PlacemarkMapObject addMarker(Point point, boolean isTemporary) {
//        PlacemarkMapObject marker = mapView.getMap().getMapObjects().addPlacemark(point);
//        IconStyle iconStyle = new IconStyle()
//                .setAnchor(new PointF(0.5f, 1.0f))
//                .setScale(0.2f);
//
//        // Установка иконки по умолчанию (для временных меток)
//        int markerIconRes = R.drawable.ic_map_marker;
//        marker.setIcon(ImageProvider.fromResource(this, markerIconRes), iconStyle);
//
//        if (!isTemporary) {
//            // Для постоянных меток добавляем обработчик
//            marker.addTapListener((mapObject, pt) -> {
//                handleMarkerClick((PlacemarkMapObject) mapObject);
//                return true;
//            });
//        }
//        return marker;
//    }
//
//    private void showCreateNoteDialog(Point point, PlacemarkMapObject marker) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
//        builder.setView(dialogView);
//
//        EditText etName = dialogView.findViewById(R.id.et_note_name);
//        EditText etNote = dialogView.findViewById(R.id.et_note_text);
//        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_note_status);
//
//        AlertDialog dialog = builder.create();
//
//        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", (d, which) -> {
//            String name = etName.getText().toString().trim();
//            String note = etNote.getText().toString().trim();
//
//            boolean hasError = false;
//
//            if (name.isEmpty()) {
//                etName.setError("Введите название");
//                hasError = true;
//            }
//
//            if (note.isEmpty()) {
//                etNote.setError("Введите заметку");
//                hasError = true;
//            }
//
//            if (!hasError) {
//                String status = rgStatus.getCheckedRadioButtonId() == R.id.rb_public ? "public" : "private";
//                saveNoteToFirebase(point, name, note, status, marker);
//                tempMarker = null;
//            } else {
//                dialog.dismiss();
//                dialog.show();
//            }
//        });
//
//        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", (d, which) -> {
//            if (marker != null) {
//                mapView.getMap().getMapObjects().remove(marker);
//                tempMarker = null;
//            }
//        });
//
//        dialog.show();
//    }
//
//    private void showExistingNoteDetails(PlacemarkMapObject mapObject) {
//        String markerId = markerIdMap.get(mapObject);
//        if (markerId == null) return;
//
//        mDatabase.child("Marks").child(markerId).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Marker marker = snapshot.getValue(Marker.class);
//                if (marker != null) {
//                    showNoteDetailsDialog(marker, mapObject);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MapMarksActivity.this,
//                        "Ошибка загрузки: " + error.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void showNoteDetailsDialog(Marker marker, PlacemarkMapObject mapObject) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note_details, null);
//
//        TextView tvName = dialogView.findViewById(R.id.tv_note_name);
//        TextView tvNote = dialogView.findViewById(R.id.tv_note_text);
//        TextView tvStatus = dialogView.findViewById(R.id.tv_note_status);
//        ImageButton btnOptions = dialogView.findViewById(R.id.btn_options);
//        ImageButton btnClose = dialogView.findViewById(R.id.btn_close);
//
//        tvName.setText(marker.getName());
//        tvNote.setText(marker.getNote());
//        tvStatus.setText(marker.getStatus().equals("public") ? "Публичная" : "Приватная");
//
//        AlertDialog dialog = builder.setView(dialogView).create();
//
//        btnClose.setOnClickListener(v -> dialog.dismiss());
//
//        btnOptions.setOnClickListener(v -> showNoteOptionsMenu(v, marker, mapObject, dialog));
//
//        dialog.show();
//    }
//
//    private void showNoteOptionsMenu(View anchor, Marker marker, PlacemarkMapObject mapObject, AlertDialog parentDialog) {
//        PopupMenu popup = new PopupMenu(this, anchor);
//        popup.getMenuInflater().inflate(R.menu.note_options_menu, popup.getMenu());
//
//        popup.setOnMenuItemClickListener(item -> {
//            if (item.getItemId() == R.id.menu_edit) {
//                showEditNoteDialog(marker, mapObject);
//                parentDialog.dismiss();
//                return true;
//            } else if (item.getItemId() == R.id.menu_delete) {
//                deleteMarker(marker, mapObject);
//                parentDialog.dismiss();
//                return true;
//            }
//            return false;
//        });
//
//        popup.show();
//    }
//
//    private void showEditNoteDialog(Marker marker, PlacemarkMapObject mapObject) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
//        builder.setView(dialogView);
//
//        EditText etName = dialogView.findViewById(R.id.et_note_name);
//        EditText etNote = dialogView.findViewById(R.id.et_note_text);
//        RadioGroup rgStatus = dialogView.findViewById(R.id.rg_note_status);
//
//        etName.setText(marker.getName());
//        etNote.setText(marker.getNote());
//        if (marker.getStatus().equals("public")) {
//            rgStatus.check(R.id.rb_public);
//        } else {
//            rgStatus.check(R.id.rb_private);
//        }
//
//        AlertDialog dialog = builder.create();
//
//        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", (d, which) -> {
//            String name = etName.getText().toString().trim();
//            String note = etNote.getText().toString().trim();
//
//            boolean hasError = false;
//
//            if (name.isEmpty()) {
//                etName.setError("Введите название");
//                hasError = true;
//            }
//
//            if (note.isEmpty()) {
//                etNote.setError("Введите заметку");
//                hasError = true;
//            }
//
//            if (!hasError) {
//                marker.setName(name);
//                marker.setNote(note);
//                marker.setStatus(rgStatus.getCheckedRadioButtonId() == R.id.rb_public ? "public" : "private");
//                updateMarkerInFirebase(marker);
//            } else {
//                dialog.dismiss();
//                dialog.show();
//            }
//        });
//
//        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", (d, which) -> {});
//        dialog.show();
//    }
//
//    private void updateMarkerInFirebase(Marker marker) {
//        mDatabase.child("Marks").child(marker.getId()).setValue(marker)
//                .addOnSuccessListener(aVoid ->
//                        Toast.makeText(this, "Метка обновлена", Toast.LENGTH_SHORT).show())
//                .addOnFailureListener(e ->
//                        Toast.makeText(this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    private void deleteMarker(Marker marker, PlacemarkMapObject mapObject) {
//        mDatabase.child("Marks").child(marker.getId()).removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    mapView.getMap().getMapObjects().remove(mapObject);
//                    markerIdMap.remove(mapObject);
//                    Toast.makeText(this, "Метка удалена", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    private void saveNoteToFirebase(Point point, String name, String note, String status, PlacemarkMapObject marker) {
//        String key = mDatabase.child("Marks").push().getKey();
//        if (key == null) return;
//
//        Marker firebaseMarker = new Marker();
//        firebaseMarker.setId(key);
//        firebaseMarker.setLatitude(point.getLatitude());
//        firebaseMarker.setLongitude(point.getLongitude());
//        firebaseMarker.setName(name);
//        firebaseMarker.setNote(note);
//        firebaseMarker.setStatus(status);
//        firebaseMarker.setTimestamp(System.currentTimeMillis());
//
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null) {
//            firebaseMarker.setUserId(user.getUid());
//        }
//
//        mDatabase.child("Marks").child(key).setValue(firebaseMarker)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show();
//                    markerIdMap.put(marker, key);
//
//                    // Обновляем иконку в зависимости от статуса
//                    int markerIconRes = status.equals("public")
//                            ? R.drawable.ic_map_marker
//                            : R.drawable.ic_map_marker_pr;
//
//                    marker.setIcon(
//                            ImageProvider.fromResource(this, markerIconRes),
//                            new IconStyle()
//                                    .setAnchor(new PointF(0.5f, 1.0f))
//                                    .setScale(0.2f)
//                    );
//
//                    marker.addTapListener((mapObject, pt) -> {
//                        handleMarkerClick((PlacemarkMapObject) mapObject);
//                        return true;
//                    });
//                })
//                .addOnFailureListener(e -> {
//                    mapView.getMap().getMapObjects().remove(marker);
//                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void loadExistingMarkers() {
//        mDatabase.child("Marks").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot markSnapshot : snapshot.getChildren()) {
//                    Marker marker = markSnapshot.getValue(Marker.class);
//                    if (marker != null) {
//                        Point point = new Point(marker.getLatitude(), marker.getLongitude());
//                        PlacemarkMapObject mapObject = addMarker(point, false);
//
//                        // Устанавливаем правильную иконку в зависимости от статуса
////                        int markerIconRes = marker.getStatus().equals("public")
////                                ? R.drawable.ic_map_marker
////                                : R.drawable.ic_map_marker_pr;
//
//                        // Исправленная проверка статуса с защитой от null
//                        String status = marker.getStatus();
//                        int markerIconRes;
//
//                        if (status != null && status.equals("public")) {
//                            markerIconRes = R.drawable.ic_map_marker;
//                        } else {
//                            markerIconRes = R.drawable.ic_map_marker_pr;
//                        }
//
//                        mapObject.setIcon(
//                                ImageProvider.fromResource(MapMarksActivity.this, markerIconRes),
//                                new IconStyle()
//                                        .setAnchor(new PointF(0.5f, 1.0f))
//                                        .setScale(0.2f)
//                        );
//
//                        markerIdMap.put(mapObject, marker.getId());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MapMarksActivity.this,
//                        "Ошибка загрузки меток: " + error.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        MapKitFactory.getInstance().onStart();
//        mapView.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        mapView.onStop();
//        MapKitFactory.getInstance().onStop();
//        super.onStop();
//    }
//}
package com.example.mysportik;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeohashTable {
    private final Map<String, List<Map<String, Object>>> geohashMap;
    private final Map<String, Map<String, Object>> markerMap; // Для быстрого доступа по ID
    private final int precision;

    public GeohashTable(int precision) {
        this.geohashMap = new HashMap<>();
        this.markerMap = new HashMap<>();
        this.precision = precision;
    }

    // Добавление метки в таблицу
    public void addMarker(double lat, double lon, Map<String, Object> markerData) {
        String geohash = GeoHashConverter.encode(lat, lon, precision);
        String markerId = (String) markerData.get("id");

        if (markerId == null) {
            markerId = "local_" + System.currentTimeMillis();
            markerData.put("id", markerId);
        }

        // Добавляем в геохэш-индекс
        geohashMap.computeIfAbsent(geohash, k -> new ArrayList<>()).add(markerData);

        // Добавляем в общий словарь
        markerMap.put(markerId, markerData);
    }

    // Получение меток по geohash
    public List<Map<String, Object>> getMarkersByGeohash(String geohash) {
        return geohashMap.getOrDefault(geohash, new ArrayList<>());
    }

    // Получение метки по ID
    public Map<String, Object> getMarkerById(String id) {
        return markerMap.get(id);
    }

//    // Поиск в радиусе (упрощенная реализация)
//    public List<Map<String, Object>> searchInRadius(double lat, double lon, double radiusKm) {
//        List<Map<String, Object>> result = new ArrayList<>();
//        String centerGeohash = GeoHashConverter.encode(lat, lon, precision);
//
//        // Получаем соседние geohash (реализация getNeighbors зависит от вашей библиотеки)
//        List<String> neighbors = getNeighboringGeohashes(centerGeohash);
//
//        for (String geohash : neighbors) {
//            result.addAll(getMarkersByGeohash(geohash));
//        }
//
//        return result;
//    }
//
//    private List<String> getNeighboringGeohashes(String geohash) {
//        // Здесь должна быть реализация получения соседних geohash
//        // Например, с использованием библиотеки geohash-java
//        List<String> neighbors = new ArrayList<>();
//        neighbors.add(geohash);
//        // Добавить логику для 8 соседних ячеек
//        return neighbors;
//    }
}

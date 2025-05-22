package com.example.mysportik;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class GeoHashTester {
    private static final String TAG = "GeoHashTest";

    public static void runAllTests() {
        testDistanceCalculation();
        testGeohashPrecision();
        testNeighboringGeohashes();
        testFullSearchScenario();
    }
    private static void testDecodeAccuracy() {
        Log.d(TAG, "===== Testing decode accuracy =====");

        // Москва
        testDecode("ucf1x3", 55.752, 37.617);
        // СПб
        testDecode("udts1j", 59.939, 30.314);
        // Экватор
        testDecode("s0000", 0, 0);
    }

    private static void testDecode(String geohash, double expectedLat, double expectedLon) {
        double[] coords = GeoHashConverter.decode(geohash);
        boolean pass = Math.abs(coords[0] - expectedLat) < 0.01 &&
                Math.abs(coords[1] - expectedLon) < 0.01;

        Log.d(TAG, String.format("%s -> %.6f,%.6f (expected %.6f,%.6f) %s",
                geohash, coords[0], coords[1], expectedLat, expectedLon,
                pass ? "✓" : "✗"));
    }




    // Тест расчета расстояния
    private static void testDistanceCalculation() {
        Log.d(TAG, "===== Testing distance calculation =====");

        // Координаты ваших меток
        double lat1 = 55.732101;
        double lon1 = 37.598765;
        double lat2 = 55.729887;
        double lon2 = 37.601990;

        double distance = Marker.calculateDistance(lat1, lon1, lat2, lon2);
        Log.d(TAG, String.format("Distance between (%.6f,%.6f) and (%.6f,%.6f) = %.3f km",
                lat1, lon1, lat2, lon2, distance));

        // Дополнительные тестовые точки
        double[][] testPoints = {
                {55.732101, 37.598765}, // Та же точка (широта, долгота)
                {55.732101, 37.608765}, // 0.6 км восточнее
                {55.742101, 37.598765}, // 1.1 км севернее
                {55.729887, 37.601990}  // Вторая тестовая точка
        };

// Правильный перебор массива:
        for (int i = 0; i < testPoints.length; i++) {
            double pointLat = testPoints[i][0]; // Широта
            double pointLon = testPoints[i][1]; // Долгота
            double dist = Marker.calculateDistance(lat1, lon1, pointLat, pointLon);
            Log.d(TAG, String.format("Distance to (%.6f,%.6f) = %.3f km",
                    pointLat, pointLon, dist));
        }
    }

    // Тест определения точности Geohash
    private static void testGeohashPrecision() {
        Log.d(TAG, "\n===== Testing geohash precision selection =====");

        double[] testRadii = {0.1, 0.5, 1, 2, 5, 10, 20, 50};

        for (double radius : testRadii) {
            int precision = Marker.calculateOptimalPrecision(radius);
            Log.d(TAG, String.format("Radius %.1f km -> Precision %d", radius, precision));
        }
    }

    // Тест поиска соседних Geohash
    private static void testNeighboringGeohashes() {
        Log.d(TAG, "\n===== Testing neighboring geohashes =====");

        String testGeohash = "u3ed1b2"; // Пример geohash для Москвы
        List<String> neighbors = Marker.getNeighboringGeohashes(testGeohash);

        Log.d(TAG, "Center geohash: " + testGeohash);
        Log.d(TAG, "Neighbors (" + neighbors.size() + "):");
        for (String neighbor : neighbors) {
            Log.d(TAG, neighbor);
        }

//        // Проверка декодирования
//        double[] center = GeoHashConverter.decode(testGeohash);
//        Log.d(TAG, String.format("Decoded center: %.6f, %.6f", center[0], center[1]));
        double[] coords = GeoHashConverter.decode("u3ed1b2");
        Log.d(TAG, String.format("Decoded: %.6f, %.6f", coords[0], coords[1]));
// Ожидаемый результат: 55.732101, 37.598765
    }

    // Тест полного сценария поиска
    private static void testFullSearchScenario() {
        Log.d(TAG, "\n===== Testing full search scenario =====");

        // Создаем тестовые метки
        List<Marker> testMarkers = new ArrayList<>();
        testMarkers.add(createTestMarker("1", 55.732101, 37.598765));
        testMarkers.add(createTestMarker("2", 55.729887, 37.601990));
        testMarkers.add(createTestMarker("3", 55.735000, 37.600000)); // ~300 м севернее
        testMarkers.add(createTestMarker("4", 55.720000, 37.610000)); // ~1.5 км южнее

        // Центр поиска - между двумя основными метками
        double centerLat = 55.731000;
        double centerLon = 37.600000;
        double[] testRadii = {0.3, 0.5, 1.0};

        for (double radius : testRadii) {
            Log.d(TAG, String.format("\nSearching at (%.6f,%.6f) with radius %.1f km",
                    centerLat, centerLon, radius));

            List<Marker> foundMarkers = Marker.findMarkersInRadius(
                    testMarkers, centerLat, centerLon, radius);

            Log.d(TAG, "Found " + foundMarkers.size() + " markers:");
            for (Marker marker : foundMarkers) {
                double dist = Marker.calculateDistance(
                        centerLat, centerLon, marker.latitude, marker.longitude);
                Log.d(TAG, String.format("- %s (%.6f,%.6f) %.3f km",
                        marker.name, marker.latitude, marker.longitude, dist));
            }
        }
    }

    private static Marker createTestMarker(String id, double lat, double lon) {
        Marker marker = new Marker();
        String markerId = marker.getId();  // Корректный доступ
        marker.name = "Test " + id;
        marker.latitude = lat;
        marker.longitude = lon;
        return marker;
    }
}
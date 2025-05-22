package com.example.mysportik;

import java.util.*;

public class GeohashStorage {

    // Ключ - геохэш строкой, значение - список меток в этой зоне
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private Map<String, List<Marker>> storage = new HashMap<>();

    // Добавление метки в хранилище
    public void addMarker(Marker marker, int precision) {
        String geohash = GeoHashConverter.encode(marker.latitude, marker.longitude, precision);
        // Если для данного геохэша нет записи, создаем новый список, добавляем метку в список
        storage.computeIfAbsent(geohash, k -> new ArrayList<>()).add(marker);
    }

    // Поиск в радиусе (возвращает список меток)
    public List<Marker> searchInRadius(double centerLat, double centerLon, double radiusKm, int precision) {
        // Получаем список геохэшей, покрывающих искомую область
        Set<String> coveringGeohashes = getGeohashesCoveringRadius(centerLat, centerLon, radiusKm, precision);

        // Собираем все метки из найденных геохэшей
        List<Marker> candidates = new ArrayList<>();
        for (String geohash : coveringGeohashes) {
            // Добавляем все метки из текущего геохэша (если есть)
            candidates.addAll(storage.getOrDefault(geohash, Collections.emptyList()));
        }

        // Точная фильтрация: оставляем только метки в заданном радиусе
        List<Marker> result = new ArrayList<>();
        for (Marker marker : candidates) {
            if (calculateDistance(centerLat, centerLon, marker.latitude, marker.longitude) <= radiusKm) {
                result.add(marker);
            }
        }
        return result;
    }

    // Метод для определения геохэшей, покрывающих заданный радиус
    private Set<String> getGeohashesCoveringRadius(double lat, double lon, double radiusKm, int precision) {
        double[] bbox = calculateBoundingBox(lat, lon, radiusKm);
        double minLat = bbox[0], maxLat = bbox[1];
        double minLon = bbox[2], maxLon = bbox[3];

        // 2. Поиск всех геохашей, пересекающихся с bounding box
        Set<String> hashes = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add("");

        while (!queue.isEmpty()) {
            String hash = queue.poll();
            double[][] cellBox = GeoHashConverter.getBoundingBox(hash);

            // Проверка пересечения ячейки с bounding box
            if (intersects(cellBox, minLat, maxLat, minLon, maxLon)) {
                if (hash.length() == precision) {
                    hashes.add(hash);
                } else {
                    // Генерация дочерних геохашей
                    for (char c : BASE32.toCharArray()) {
                        queue.add(hash + c);
                    }
                }
            }
        }
        return hashes;
    }

    // Расчет bounding box радиуса
    private double[] calculateBoundingBox(double lat, double lon, double radiusKm) {
        final double R = 6371.0;
        double deltaLat = Math.toDegrees(radiusKm / R);
        double deltaLon = Math.toDegrees(radiusKm / (R * Math.cos(Math.toRadians(lat))));

        return new double[] {
                lat - deltaLat,  // minLat
                lat + deltaLat,  // maxLat
                lon - deltaLon,  // minLon
                lon + deltaLon   // maxLon
        };
    }

    // Проверка пересечения двух прямоугольников
    private boolean intersects(double[][] cellBox, double minLat, double maxLat, double minLon, double maxLon) {

        double cellMinLat = cellBox[0][0];
        double cellMaxLat = cellBox[0][1];
        double cellMinLon = cellBox[1][0];
        double cellMaxLon = cellBox[1][1];

        return !(cellMaxLat < minLat ||
                cellMinLat > maxLat ||
                cellMaxLon < minLon ||
                cellMinLon > maxLon);

        /*return !(cellBox[0][1] < minLat ||
                cellBox[0][0] > maxLat ||
                cellBox[1][1] < minLon ||
                cellBox[1][0] > maxLon);*/
    }

    // Расчет расстояния между двумя точками по формуле гаверсинусов
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Радиус Земли в километрах

        // Разница координат в радианах
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Формула гаверсинусов:
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // Расчет расстояния
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

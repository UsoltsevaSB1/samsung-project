package com.example.mysportik;
import java.util.ArrayList;
import java.util.List;

public class Marker {
    private String id;
    public double lat;
    public double lon;
    public String name;
    private String note;
    private String status; // "public" или "private"
    private String userId;
    private long timestamp;
    private transient String geohashCache;
//
    public Marker() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return lat; }
    public double getLongitude() { return lon; }
    public String getNote() { return note; }
    public String getStatus() { return status; }


    // Добавляем сеттер, если нужно изменять id
    public void setId(String id) {
        this.id = id;
    }

    // Основной метод поиска
    public static List<Marker> findMarkersInRadius(List<Marker> allMarkers,
                                                   double centerLat,
                                                   double centerLon,
                                                   double radiusKm) {
        if (radiusKm < 1) {
            return findMarkersInRadiusExact(allMarkers, centerLat, centerLon, radiusKm);
        } else if (radiusKm < 20) {
            return findMarkersInRadiusOptimized(allMarkers, centerLat, centerLon, radiusKm);
        } else {
            int precision = calculateOptimalPrecision(radiusKm);
            return findMarkersInRadiusGeoHash(allMarkers, centerLat, centerLon, radiusKm, precision);
        }
    }

    // 1. Точный метод (Haversine)
    public static List<Marker> findMarkersInRadiusExact(List<Marker> markers,
                                                         double centerLat,
                                                         double centerLon,
                                                         double radiusKm) {
        List<Marker> result = new ArrayList<>();
        for (Marker marker : markers) {
            if (calculateDistance(centerLat, centerLon, marker.lat, marker.lon) <= radiusKm) {
                result.add(marker);
            }
        }
        return result;
    }

    // 2. Оптимизированный метод (Bounding Box)
    public static List<Marker> findMarkersInRadiusOptimized(List<Marker> markers,
                                                             double centerLat,
                                                             double centerLon,
                                                             double radiusKm) {
        List<Marker> result = new ArrayList<>();
        double[][] bounds = calculateBoundingBox(centerLat, centerLon, radiusKm);

        for (Marker marker : markers) {
            if (marker.lat >= bounds[0][0] && marker.lat <= bounds[0][1] &&
                    marker.lon >= bounds[1][0] && marker.lon <= bounds[1][1]) {

                if (calculateDistance(centerLat, centerLon, marker.lat, marker.lon) <= radiusKm) {
                    result.add(marker);
                }
            }
        }
        return result;
    }

    // 3. Geohash-метод
    public static List<Marker> findMarkersInRadiusGeoHash(List<Marker> markers,
                                                           double centerLat,
                                                           double centerLon,
                                                           double radiusKm,
                                                           int precision) {
        List<Marker> result = new ArrayList<>();
        String centerGeohash = GeoHashConverter.encode(centerLat, centerLon, precision);

        // Получаем соседние геохеши
        List<String> neighbors = getNeighboringGeohashes(centerGeohash);
        neighbors.add(centerGeohash);

        for (Marker marker : markers) {
            String markerGeohash = marker.getGeohash(precision);
            if (neighbors.contains(markerGeohash)) {
                if (calculateDistance(centerLat, centerLon, marker.lat, marker.lon) <= radiusKm) {
                    result.add(marker);
                }
            }
        }
        return result;
    }

    // Вспомогательные методы -------------------------------------------------

    // Расчет расстояния по Haversine
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Радиус Земли в км

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Расчет bounding box
    public static double[][] calculateBoundingBox(double lat, double lon, double radiusKm) {
        double deltaLat = (radiusKm / 6371.0) * (180 / Math.PI);
        double deltaLon = deltaLat / Math.cos(Math.toRadians(lat));
        return new double[][] {
                {lat - deltaLat, lat + deltaLat}, // Широты
                {lon - deltaLon, lon + deltaLon}  // Долготы
        };
    }

//    // Получение соседних геохешей
//    public static List<String> getNeighboringGeohashes(String geohash) {
//        List<String> neighbors = new ArrayList<>();
//        double[][] bbox = GeoHashConverter.getBoundingBox(geohash);
//        double latSize = bbox[0][1] - bbox[0][0];
//        double lonSize = bbox[1][1] - bbox[1][0];
//        double[] center = GeoHashConverter.decode(geohash);
//
//        // 8 направлений
//        double[] offsets = {-1, 0, 1};
//        for (double latOffset : offsets) {
//            for (double lonOffset : offsets) {
//                if (latOffset == 0 && lonOffset == 0) continue;
//
//                double neighborLat = center[0] + latOffset * latSize;
//                double neighborLon = center[1] + lonOffset * lonSize;
//
//                if (Math.abs(neighborLat) <= 90 && Math.abs(neighborLon) <= 180) {
//                    neighbors.add(GeoHashConverter.encode(
//                            neighborLat, neighborLon, geohash.length()));
//                }
//            }
//        }
//        return neighbors;
//    }
//
//
public static List<String> getNeighboringGeohashes(String geohash) {
    List<String> neighbors = new ArrayList<>();
    int precision = geohash.length();
    double[] center = GeoHashConverter.decode(geohash);
    double[][] bbox = GeoHashConverter.getBoundingBox(geohash);

    // Размеры текущей ячейки
    double latSize = bbox[0][1] - bbox[0][0];
    double lonSize = bbox[1][1] - bbox[1][0];

    // Проверяем 8 соседних направлений
    for (double latOffset = -1; latOffset <= 1; latOffset++) {
        for (double lonOffset = -1; lonOffset <= 1; lonOffset++) {
            if (latOffset == 0 && lonOffset == 0) continue;

            double neighborLat = center[0] + latOffset * latSize;
            double neighborLon = center[1] + lonOffset * lonSize;

            // Корректируем координаты
            neighborLat = Math.max(-90, Math.min(90, neighborLat));
            neighborLon = (neighborLon + 180) % 360 - 180; // Нормализуем долготу

            String neighborHash = GeoHashConverter.encode(neighborLat, neighborLon, precision);
            if (!neighborHash.equals(geohash)) {
                neighbors.add(neighborHash);
            }
        }
    }
    return neighbors;
}
    // Коррекция широты (должна быть между -90 и 90)
    private static double normalizeLatitude(double lat) {
        if (lat > 90) return 180 - lat;
        if (lat < -90) return -180 - lat;
        return lat;
    }

    // Коррекция долготы (должна быть между -180 и 180)
    private static double normalizeLongitude(double lon) {
        lon = lon % 360; // Нормализуем до (-360, 360)
        if (lon > 180) lon -= 360;
        if (lon < -180) lon += 360;
        return lon;
    }

    // Определение точности геохеша
    public static int calculateOptimalPrecision(double radiusKm) {
        if (radiusKm >= 20) return 4;  // ~20 км
        if (radiusKm >= 5)  return 5;  // ~5 км
        if (radiusKm >= 1)  return 6;  // ~1 км
        if (radiusKm >= 0.2) return 7; // ~150 м
        return 8; // ~20 м (для очень малых радиусов)
    }

    // Кэшированный геохеш
    public String getGeohash(int precision) {
        if (geohashCache == null || geohashCache.length() != precision) {
            geohashCache = GeoHashConverter.encode(lat, lon, precision);
        }
        return geohashCache;
    }

}

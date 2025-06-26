package com.example.mysportik;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;

public class Marker implements Parcelable{
    private String id;
    private String geohash;
    public double latitude;
    public double longitude;
    public String name;
    private String note;
    private String status; // "public" или "private"
    private long timestamp;
    private String userId;
    //private transient String geohashCache;
//
    public Marker() {}

    // Конструктор для Parcelable
    protected Marker(Parcel in) {
        id = in.readString();
        geohash = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        name = in.readString();
        note = in.readString();
        status = in.readString();
        timestamp = in.readLong();
        userId = in.readString();
    }


    // Обязательное поле CREATOR
    public static final Creator<Marker> CREATOR = new Creator<Marker>() {
        @Override
        public Marker createFromParcel(Parcel in) {
            return new Marker(in);  // Создает объект из Parcel
        }

        @Override
        public Marker[] newArray(int size) {
            return new Marker[size];  // Создает массив объектов
        }
    };

    // Обязательный метод (обычно возвращает 0)
    @Override
    public int describeContents() {
        return 0;
    }

    // Записывает поля объекта в Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(geohash);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(name);
        dest.writeString(note);
        dest.writeString(status);
        dest.writeLong(timestamp);
        dest.writeString(userId);

    }

    // Геттеры и сеттеры для всех полей
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getGeohash() { return geohash; }
    public void setGeohash(String geohash) { this.geohash = geohash; }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("name", name);
        result.put("note", note);
        result.put("status", status);
        result.put("userId", userId);
        result.put("timestamp", timestamp);
        result.put("geohash", geohash);
        return result;
    }

    /*public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getNote() { return note; }
    public String getStatus() { return status; }
    public void getNote(String note) {this.note = note;}


    // Добавляем сеттер, если нужно изменять id
    public void setId(String id) {
        this.id = id;
    }
    public void setLatitude(double latitude) { this.latitude = this.latitude; }
    public void setLongitude(double longitude) { this.longitude = this.longitude; }
    public void setName(String s) { this.name = name; }
    public void setNote(String note) {
        this.note = note;
    }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setGeohash(String geohash) { this.geohash = geohash; }
    */

    /*public void setStatus(String status) {
        if ("public".equals(status) || "private".equals(status)) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid status. Must be 'public' or 'private'");
        }
    }*/


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
            if (calculateDistance(centerLat, centerLon, marker.latitude, marker.longitude) <= radiusKm) {
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
            if (marker.latitude >= bounds[0][0] && marker.latitude <= bounds[0][1] &&
                    marker.longitude >= bounds[1][0] && marker.longitude <= bounds[1][1]) {

                if (calculateDistance(centerLat, centerLon, marker.latitude, marker.longitude) <= radiusKm) {
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
            String markerGeohash = GeoHashConverter.encode(marker.getLatitude(), marker.getLongitude(), precision);
            if (neighbors.contains(markerGeohash)) {
                double distance = calculateDistance(centerLat, centerLon,
                        marker.getLatitude(),
                        marker.getLongitude());
                if (distance <= radiusKm) {
                    result.add(marker);
                }
            }
        }
        return result;
    }

    // Вспомогательные методы -------------------------------------------------

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Радиус Земли в километрах

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
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
    /*public String getGeohash(int precision) {
        if (geohashCache == null || geohashCache.length() != precision) {
            geohashCache = GeoHashConverter.encode(latitude, longitude, precision);
        }
        return geohashCache;
    }*/
}

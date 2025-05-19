package com.example.mysportik;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class GeoHashConverter {
    //стандартный алфавит для геохэша
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    // Каждый символ геохэша кодирует 5 бит информации
    private static final int BITS_PER_CHAR = 5;

    public static String encode(double lat, double lon, int precision) {
        double[] latInterval = {-90.0, 90.0};
        double[] lonInterval = {-180.0, 180.0};
        StringBuilder hash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (hash.length() < precision) {
            // Если обрабатываем долготу (четный шаг)
            if (isEven) {
                // Находим середину текущего интервала долготы
                double mid = (lonInterval[0] + lonInterval[1]) / 2;
                // Если долгота больше середины
                if (lon > mid) {
                    // Устанавливаем соответствующий бит
                    ch |= (1 << (4 - bit));
                    // Сужаем интервал до верхней половины
                    lonInterval[0] = mid;
                } else {
                    //сужаем интервал до нижней половины
                    lonInterval[1] = mid;
                }
            } else {
                // Обрабатываем широту (нечетный шаг)
                // Находим середину текущего интервала широты
                double mid = (latInterval[0] + latInterval[1]) / 2;
                if (lat > mid) {
                    ch |= (1 << (4 - bit));
                    latInterval[0] = mid;
                } else {
                    latInterval[1] = mid;
                }
            }
            // Переключаем флаг для следующей итерации (широта/долгота)
            isEven = !isEven;
            if (bit < 4) {
                // Если еще не набрали 5 бит, увеличиваем счетчик
                bit++;
            } else {
                // Если набрали 5 бит, добавляем соответствующий символ из BASE32
                hash.append(BASE32.charAt(ch));
                // Сбрасываем счетчик битов и накопленные биты
                bit = 0;
                ch = 0;
            }
        }
        return hash.toString();
    }

    //Декодирует геохаш в координаты (центр ячейки)
    public static double[] decode(String geohash) {
        boolean isEven = true;
        double[] latInterval = {-90.0, 90.0};
        double[] lonInterval = {-180.0, 180.0};

        for (char c : geohash.toCharArray()) {
            int val = BASE32.indexOf(c);
            for (int i = 0; i < BITS_PER_CHAR; i++) {
                int mask = 1 << (4 - i);
                if (isEven) {
                    refineInterval(lonInterval, (val & mask) != 0);
                } else {
                    refineInterval(latInterval, (val & mask) != 0);
                }
                isEven = !isEven;
            }
        }
        return new double[] {
                (latInterval[0] + latInterval[1]) / 2,
                (lonInterval[0] + lonInterval[1]) / 2
        };
    }

    //Уточняет интервал на основе текущего бита
    private static void refineInterval(double[] interval, boolean bit) {
        double mid = (interval[0] + interval[1]) / 2;
        if (bit) {
            interval[0] = mid;
        } else {
            interval[1] = mid;
        }
    }

    //Рассчитывает bounding box для геохаша, аналогично decode но возвращает гарницы вместо центра
    public static double[][] getBoundingBox(String geohash) {
        double[] latInterval = {-90.0, 90.0};
        double[] lonInterval = {-180.0, 180.0};
        boolean isEven = true;

        for (char c : geohash.toCharArray()) {
            int val = BASE32.indexOf(c);
            for (int i = 0; i < BITS_PER_CHAR; i++) {
                int mask = 1 << (4 - i);
                if (isEven) {
                    refineInterval(lonInterval, (val & mask) != 0);
                } else {
                    refineInterval(latInterval, (val & mask) != 0);
                }
                isEven = !isEven;
            }
        }
        return new double[][]{latInterval, lonInterval};
    }
}

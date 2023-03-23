package com.bcgg;

import java.util.Random;

public class RandomCoordinatesGenerator {
    
    private static final double CHUNGCHEONGNAM_LAT_MIN = 39.0;
    private static final double CHUNGCHEONGNAM_LAT_MAX = 41.0;
    private static final double CHUNGCHEONGNAM_LON_MIN = 126.0;
    private static final double CHUNGCHEONGNAM_LON_MAX = 127.5;

    public static void main(String[] args) {
        Random random = new Random();
        for (int i = 1; i <= 5; i++) {
            double lat = CHUNGCHEONGNAM_LAT_MIN + (CHUNGCHEONGNAM_LAT_MAX - CHUNGCHEONGNAM_LAT_MIN) * random.nextDouble();
            double lon = CHUNGCHEONGNAM_LON_MIN + (CHUNGCHEONGNAM_LON_MAX - CHUNGCHEONGNAM_LON_MIN) * random.nextDouble();
            System.out.println("Random coordinate " + i + ": (" + lat + ", " + lon + ")");
        }
    }
}

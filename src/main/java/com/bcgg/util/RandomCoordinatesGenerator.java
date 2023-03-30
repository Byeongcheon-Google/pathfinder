package com.bcgg.util;

import com.bcgg.pathgenerator.model.Spot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCoordinatesGenerator {

    private static final double CHUNGCHEONGNAM_LAT_MIN = 36.0;
    private static final double CHUNGCHEONGNAM_LAT_MAX = 45.0;
    private static final double CHUNGCHEONGNAM_LON_MIN = 122.0;
    private static final double CHUNGCHEONGNAM_LON_MAX = 127.5;

    public static List<Spot> generate(int count) {
        List<Spot> spots = new ArrayList<>();
        Random random = new Random();
        for (int i = 1; i <= count; i++) {
            double lat = CHUNGCHEONGNAM_LAT_MIN + (CHUNGCHEONGNAM_LAT_MAX - CHUNGCHEONGNAM_LAT_MIN) * random.nextDouble();
            double lon = CHUNGCHEONGNAM_LON_MIN + (CHUNGCHEONGNAM_LON_MAX - CHUNGCHEONGNAM_LON_MIN) * random.nextDouble();
            spots.add(new Spot.Tour(lat, lon));
        }

        return spots;
    }
}

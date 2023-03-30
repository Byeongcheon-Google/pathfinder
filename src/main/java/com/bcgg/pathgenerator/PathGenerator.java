package com.bcgg.pathgenerator;

import com.bcgg.pathgenerator.model.Spot;
import com.bcgg.util.DistanceCalculator;
import com.bcgg.util.MidpointCalculator;
import com.bcgg.util.SpotUtil;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.*;

public class PathGenerator {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }

    //36.7607037 127.2808577 한기대 참빛관
    //36.7655739 127.2823278 담헌

    public static void run(
            List<Spot> spotList,
            LocalTime startTime,
            LocalTime endTime
    ) {
        // 1 Generate group
        generateGroup(spotList);
    }

    @NotNull
    public static Map<Spot.K, List<Spot>> generateGroup(@NotNull List<Spot> spotList) {
        int kMeansRepeat = 5;
        int kCount = 1;

        Spot midSpot = new Spot.Normal(
                MidpointCalculator.calculate(spotList.stream().map(Spot::getLatitude).toList()),
                MidpointCalculator.calculate(spotList.stream().map(Spot::getLongitude).toList())
        );

        Spot longestPoint = SpotUtil.getLongestSpot(spotList, midSpot);
        Map<Spot.K, List<Spot>> result = null;
        int maxCount = Integer.MAX_VALUE;

        while (maxCount > 5) {
            maxCount = 0;
            var kSpots = SpotUtil.generateKSpots(midSpot, longestPoint, kCount++);

            // k-mean clustering algorithm
            result = kMeansAlgorithm(kMeansRepeat, spotList, kSpots);

            for (var spot : result.keySet()) {
                int size = result.get(spot).size();
                if (maxCount < size) maxCount = size;
            }
        }

        result.remove(new Spot.K(Double.NaN, Double.NaN));

        return result;
    }

    @NotNull
    private static Map<Spot.K, List<Spot>> kMeansAlgorithm(
            int kMeansRepeat,
            @NotNull List<Spot> spotList,
            @NotNull List<Spot.K> initialKSpots
    ) {
        List<double[]> kSpots = initialKSpots
                .stream()
                .map((k -> new double[]{k.getLatitude(), k.getLongitude()}))
                .toList();

        boolean[][] rnk = new boolean[spotList.size()][kSpots.size()];

        for (int i = 0; i < kMeansRepeat; i++) {
            rnk = new boolean[spotList.size()][kSpots.size()];
            //Assignment step
            for (int n = 0; n < spotList.size(); n++) {
                int minK = 0;
                double minDistance = Double.MAX_VALUE;

                for (int k = 0; k < kSpots.size(); k++) {

                    double distance = DistanceCalculator.getDistance(
                            kSpots.get(k)[0],
                            kSpots.get(k)[1],
                            spotList.get(n).getLatitude(),
                            spotList.get(n).getLongitude()
                    );

                    if (minDistance > distance) {
                        minK = k;
                        minDistance = distance;
                    }
                }

                rnk[n][minK] = true;
            }

            //Update step
            for (int k = 0; k < kSpots.size(); k++) {
                int rnkSum = 0;
                double rnkxnX = 0;
                double rnkxnY = 0;
                for (int j = 0; j < spotList.size(); j++) {
                    rnkSum += rnk[j][k] ? 1 : 0;
                    rnkxnX += (rnk[j][k] ? 1 : 0) * spotList.get(j).getLatitude();
                    rnkxnY += (rnk[j][k] ? 1 : 0) * spotList.get(j).getLongitude();
                }
                kSpots.get(k)[0] = (1.0 / rnkSum) * rnkxnX;
                kSpots.get(k)[1] = (1.0 / rnkSum) * rnkxnY;
            }
        }


        //Generate result Map
        Map<Spot.K, List<Spot>> result = new HashMap<>();

        for (int k = 0; k < kSpots.size(); k++) {
            Spot.K kSpot = new Spot.K(kSpots.get(k)[0], kSpots.get(k)[1]);
            List<Spot> spots = new ArrayList<>();

            for (int j = 0; j < spotList.size(); j++) {
                if (rnk[j][k]) {
                    spots.add(spotList.get(j));
                }
            }

            result.put(kSpot, spots);
        }

        return result;
    }
}
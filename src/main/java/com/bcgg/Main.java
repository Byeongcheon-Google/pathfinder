package com.bcgg;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {
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
        generateGroup(spotList);
    }

    public static void generateGroup(List<Spot> spotList) {
        System.out.println("[Spot Lat, Lon]");
        for(int n = 0; n < spotList.size(); n++) {
            System.out.printf("k%d : (%f, %f)\n", n + 1, spotList.get(n).getLatitude(), spotList.get(n).getLongitude());
        }

        int I = 5;

        double midLatitude = MidpointCalculator.INSTANCE.calculate(spotList.stream().map(Spot::getLatitude).toList());
        double midLongitude = MidpointCalculator.INSTANCE.calculate(spotList.stream().map(Spot::getLongitude).toList());

        //먼 좌표 구하기
        Spot longestPoint = spotList.get(0);
        double longestDistance = 0.0;

        for (Spot spot : spotList) {
            double distance = DistanceCalculator.distance(midLatitude, midLongitude, spot.getLatitude(), spot.getLongitude());
            if (distance > longestDistance) {
                longestPoint = spot;
                longestDistance = distance;
            }
        }

        // k 구하기
        int kCount = 2;

        List<double[]> kSpot = new ArrayList<>();

        kSpot.add(new double[]{longestPoint.getLatitude(), longestPoint.getLongitude()});

        for (int i = 1; i < kCount; i++) {
            double lat = longestPoint.getLatitude() - midLatitude;
            double lon = longestPoint.getLongitude() - midLongitude;
            double[] vector = new double[]{lat, lon};

            double[] rotated = rotateVector(vector, (2 * Math.PI / kCount) * i);
            rotated[0] += midLatitude;
            rotated[1] += midLongitude;

            kSpot.add(rotated);
        }

        System.out.println("[Midpoint]");
        System.out.printf("(%f, %f)\n", midLatitude, midLongitude);


        System.out.println("[Initial K positions]");
        for(int k = 0; k < kSpot.size(); k++) {
            System.out.printf("k%d : (%f, %f)\n", k + 1, kSpot.get(k)[0], kSpot.get(k)[1]);
        }

        // k-mean clustering algorithm
        for (int i = 0; i < I; i++) {
            boolean[][] rnk = new boolean[spotList.size()][kSpot.size()];
            //Assignment step
            for (int n = 0; n < spotList.size(); n++) {
                int minK = 0;
                double minDistance = Double.MAX_VALUE;

                for (int k = 0; k < kSpot.size(); k++) {
                    double distance = DistanceCalculator.distance(kSpot.get(k)[0], kSpot.get(k)[1], spotList.get(n).getLatitude(), spotList.get(n).getLongitude());
                    if(minDistance > distance) {
                        minK = k;
                        minDistance = distance;
                    }
                }

                rnk[n][minK] = true;
            }

            //Update step
            for(int k = 0; k < kSpot.size(); k++) {
                int rnkSum = 0;
                double rnkxnX = 0;
                double rnkxnY = 0;
                for(int j = 0; j < spotList.size(); j++) {
                    rnkSum += rnk[j][k] ? 1 : 0;
                    rnkxnX += (rnk[j][k] ? 1 : 0) * spotList.get(j).getLatitude();
                    rnkxnY += (rnk[j][k] ? 1 : 0) * spotList.get(j).getLongitude();
                }
                kSpot.get(k)[0] = (1.0 / rnkSum) * rnkxnX;
                kSpot.get(k)[1] = (1.0 / rnkSum) * rnkxnY;
            }

            System.out.printf("[Updated K positions #%d]\n", i + 1);
            for(int k = 0; k < kSpot.size(); k++) {
                System.out.printf("k%d : (%f, %f)\n", k + 1, kSpot.get(k)[0], kSpot.get(k)[1]);
            }
        }
    }

    public static double[] rotateVector(double[] vector, double angle) {
        double[] rotatedVector = new double[2];
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        rotatedVector[0] = vector[0] * cosAngle - vector[1] * sinAngle;
        rotatedVector[1] = vector[0] * sinAngle + vector[1] * cosAngle;

        return rotatedVector;
    }
}
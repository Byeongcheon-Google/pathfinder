package com.bcgg.shortRoute;

import com.bcgg.pathgenerator.model.Spot;
import com.bcgg.util.DistanceCalculator;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ShortRoute {

    public static int nodeNum;
    public static int edgeNum;
    public static Spot startSpot;
    public static Spot endSpot;


    public ShortRoute(int nodeNum, int edgeNum, Spot.House startSpot, Spot.K endSpot){
        this.nodeNum = nodeNum;
        this.edgeNum = edgeNum;
        this.startSpot = startSpot;
        this.endSpot = endSpot;
    }

    public static ArrayList<ArrayList<QueueNode>> graph = new ArrayList<ArrayList<QueueNode>>();

    public static double[] d;

    /**
     * 숙소의 갯수는 2개로 고정.
     * -> 하나여도 한개를 복사해서 사용
     * @param spotList
     */
    public static void init(List<Spot.K> spotList, Spot.House startSpot) {
        d = new double[spotList.size()+1];
        DistanceCalculator distanceCalculator = new DistanceCalculator();

        nodeNum = spotList.size() + 2;
        edgeNum = spotList.size()* (spotList.size()-1) / 2;

        for (int i = 0; i <= nodeNum; i++) {
            graph.add(new ArrayList<QueueNode>());
        }

        for (int j = 1; j <spotList.size(); j++) {
            graph.get(0).add(new QueueNode(j,
                    DistanceCalculator.getDistance(
                            startSpot.getLatitude(),
                            startSpot.getLongitude(),
                            spotList.get(j-1).getLatitude(),
                            spotList.get(j-1).getLongitude()
                    )));

        }

        for (int i = 1; i <spotList.size()+1; i++) {
            for (int j = i+1; j <spotList.size(); j++) {
                graph.get(i).add(new QueueNode(j,
                        DistanceCalculator.getDistance(
                                spotList.get(i).getLatitude(),
                                spotList.get(i).getLongitude(),
                                spotList.get(j).getLatitude(),
                                spotList.get(j).getLongitude()
                        )));

            }
        }

        Arrays.fill(d,Double.MAX_VALUE);
    }

    public static void dijkstra(){

        PriorityQueue<QueueNode> pQue = new PriorityQueue<>();

        pQue.offer(new QueueNode(0,0.0));

        d[0] = 0;

        while (!pQue.isEmpty()){

            QueueNode queueNode =pQue.poll();

            double dist = queueNode.getDistance();
            int now = queueNode.getIndex();
            if (d[now] < dist){
                continue;
            }

            for (int i = 0; i <graph.get(now).size(); i++) {
                double cost = d[now] + graph.get(now).get(i).getDistance();

                if (cost < d[graph.get(now).get(i).getIndex()]) {
                    d[graph.get(now).get(i).getIndex()] = cost;
                    pQue.offer(new QueueNode(graph.get(now).get(i).getIndex(), cost));
                }
            }
        }
    }

    public static List<Spot> result(List<Spot.K> spotList, Spot.House startSpot) {

        Stream<Double> doubleStream = IntStream.range(0, nodeNum + 1).filter(i -> d[i] != Double.MAX_VALUE).mapToObj(i -> d[i]);
        List<Spot> listSpot = new ArrayList<>();

        double[] dClone = d.clone();

        Arrays.sort(dClone);
        listSpot.add(startSpot);
        int storeJ =0 ;
        for (int i = 1; i <dClone.length; i++) {
            for (int j = 1; j <spotList.size()+1 ; j++) {
                if (dClone[i] == d[j])
                  storeJ = j;

            }
            listSpot.add(spotList.get(storeJ-1));
        }

        return listSpot;
    }



}

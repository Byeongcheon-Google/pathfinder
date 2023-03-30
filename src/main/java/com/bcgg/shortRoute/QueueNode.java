package com.bcgg.shortRoute;

import com.bcgg.pathgenerator.model.Spot;
import org.jetbrains.annotations.NotNull;

public class QueueNode implements Comparable<QueueNode> {

    private int index;
    private double distance;

    public QueueNode(int index, double distance){
        this.index = index;
        this.distance = distance;
    }

    public int getIndex(){
        return this.index;
    }

    public double getDistance(){
        return this.distance;
    }

    @Override
    public int compareTo(@NotNull QueueNode o) {
        return (int)((this.distance-o.distance) * 10_000_000);
    }
}

package com.bcgg.mvc;

import com.bcgg.api.response.DirectionsResponse;
import com.bcgg.di.ServiceLocator;
import com.bcgg.model.PathFinderInput;
import com.bcgg.model.Point;

import com.bcgg.pathfinder.PathFinder;
import com.bcgg.pathfinder.PathFinderResult;
import com.bcgg.pathfinder.PathFinderState;

import com.bcgg.util.LocalTimeTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.reactivex.rxjava3.functions.Consumer;
import kotlin.Pair;
import kotlin.ranges.ClosedRange;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;



@Service
@RequiredArgsConstructor
public class PathFinderService {

    PathFinder pathFinder;

    public String createPathFinder(PathFinderInput pathFinderInput){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm");
        this.pathFinder  = new PathFinder(ServiceLocator.INSTANCE.getDirectionsRepository(), pathFinderInput);
        final String[] response = {""};
        pathFinder.getPath().subscribe(
                new Subscriber<PathFinderState>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(PathFinderState pathFinderState) {

                        if (pathFinderState instanceof PathFinderState.Finding) {

                            System.out.print("\r");
                            PathFinderState.Finding findingState = (PathFinderState.Finding) pathFinderState;

                            double progress = findingState.getSearchEdgesCount() / (double) findingState.getAllEdgesCount() * 100;

                            System.out.printf("%.2f%% (%d / %d)",
                                    progress,
                                    findingState.getSearchEdgesCount(),
                                    findingState.getAllEdgesCount()
                            );

                        } else if (pathFinderState instanceof PathFinderState.Found) {

                            System.out.println();
                            PathFinderState.Found foundState = (PathFinderState.Found) pathFinderState;
                            long endTime = System.nanoTime();
                            List<Pair<Point, ClosedRange<LocalTime>>> path = foundState.getPath();

                            List<String> result = new ArrayList<>();
                            for (int i = 0; i < path.size(); i++) {
                                Pair<Point, ClosedRange<LocalTime>> entry = path.get(i);
                                Point point = entry.getFirst();
                                ClosedRange<LocalTime> timeRange = entry.getSecond();

                                String formattedString = String.format("#%d : %s\t\t%s ~ %s",
                                        i + 1,
                                        point,
                                        timeRange.getStart().format(format),
                                        timeRange.getEndInclusive().format(format));

                                result.add(formattedString);
                            }

                            String joinedResult = String.join("\n", result);
                            System.out.println(joinedResult);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
//                        throw  new PathFinderException(t.getMessage());
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        pathFinder.getResult().subscribe(
                                new Consumer<PathFinderResult>() {
                                    @Override
                                    public void accept(PathFinderResult pathFinderResult) throws Throwable {
                                        Gson gson = ServiceLocator.INSTANCE.getGson();
                                        String s = gson.toJson(pathFinderResult);
                                        System.out.println(gson.toJson(pathFinderResult));
                                        response[0] = s;
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Throwable {
//                                        throw new PathFinderException(throwable.getMessage());
                                        throwable.printStackTrace();
                                    }
                                }
                        );


                    }
                }
        );

        return response[0];
    }



}

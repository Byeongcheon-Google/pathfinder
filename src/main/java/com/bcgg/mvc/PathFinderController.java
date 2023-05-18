package com.bcgg.mvc;

import com.bcgg.model.PathFinderInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class PathFinderController {

    private PathFinderService pathFinderService;
    @Autowired
    public void PathFinderService(PathFinderService pathFinderService){
        this.pathFinderService = pathFinderService;
    }


    @PostMapping
    public ResponseEntity<?> createPathFinder(
            @RequestBody PathFinderInput pathFinderInput
    ){
        String result = pathFinderService.createPathFinder(pathFinderInput);

        return ResponseEntity.ok(result);
    }
}

package com.bcgg.mvc.exception;

import lombok.Getter;

@Getter
public class PathFinderException extends RuntimeException {

    private String errorMessage;

    public PathFinderException(String errorMessage){
        this.errorMessage = errorMessage;
    }
}

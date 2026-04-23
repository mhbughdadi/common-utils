package com.apogee.common.exceptions;

public class MapperException extends RuntimeException {

    public MapperException(String message) {
        this(message,null);
    }

    public MapperException(String message, Throwable cause) {
        super(message, cause);
    }

}

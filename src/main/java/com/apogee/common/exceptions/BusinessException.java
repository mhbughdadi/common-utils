package com.apogee.common.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final String errorDescription;
    private final String errorDescriptionAr;


    public BusinessException(String message, String errorCode, String errorDescription, String errorDescriptionAr, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.errorDescriptionAr = errorDescriptionAr;
    }

    public BusinessException(String message, String errorCode) {
        this(message, errorCode, null, null, null);

    }
}

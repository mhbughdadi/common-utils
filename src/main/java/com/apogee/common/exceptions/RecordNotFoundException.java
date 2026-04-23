package com.apogee.common.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class RecordNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long recordId;
    private final Long[] recordIds;


    public RecordNotFoundException(String message, Long recordId) {
        super(message, null);
        this.recordId = recordId;
        this.recordIds = null;
    }

    public RecordNotFoundException(String message, Long... recordIds) {
        super(message, null);
        this.recordId = null;
        this.recordIds = recordIds;
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.recordId = null;
        this.recordIds = null;
    }

    public RecordNotFoundException(Throwable cause) {
        this(null, cause);
    }
}

package com.apogee.common.exceptions;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;


/**
 * Custom exception for database-related errors.
 */
@Getter
public class DBException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * -- GETTER --
     *  Gets the record IDs related to the exception.
     *
     * @return the record IDs, or null if not set
     */
    private final Long[] recordIds;
    /**
     * -- GETTER --
     *  Gets the entity class related to the exception.
     *
     * @return the entity class, or null if not set
     */
    private final Class<?> entityClass;

    /**
     * Constructs a new DBException with the specified detail message, entity class, and record IDs.
     *
     * @param message    the detail message
     * @param entityClass the class of the entity related to the exception
     * @param recordIds  the IDs of the records related to the exception
     */
    public DBException(String message, Class<?> entityClass, Long... recordIds) {
        super(message);
        this.recordIds = recordIds;
        this.entityClass = entityClass;
    }

    /**
     * Constructs a new DBException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public DBException(String message, Throwable cause) {
        super(message, cause);
        this.recordIds = null;
        this.entityClass = null;
    }

    /**
     * Constructs a new DBException with the specified cause.
     *
     * @param cause the cause
     */
    public DBException(Throwable cause) {
        super(cause);
        this.recordIds = null;
        this.entityClass = null;
    }

}

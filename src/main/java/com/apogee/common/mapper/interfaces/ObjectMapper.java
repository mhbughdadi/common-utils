package com.apogee.common.mapper.interfaces;

/**
 * Interface for object mapping functionality.
 * Provides methods to map objects from source to destination types.
 */
public interface ObjectMapper {

    /**
     * Maps the source object to the specified destination class.
     *
     * @param source the source object to map from
     * @param destinationClass the class of the destination object
     * @param <S> the type of the source object
     * @param <D> the type of the destination object
     * @return the mapped destination object
     * @throws Exception if mapping fails
     */
    <S, D> D map(S source, Class<D> destinationClass) throws Exception;
}

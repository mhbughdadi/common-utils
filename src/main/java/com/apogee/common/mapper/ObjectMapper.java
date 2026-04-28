package com.apogee.common.mapper;

import com.apogee.common.exceptions.MapperException;
import com.apogee.common.mapper.interfaces.ThrowingBiFunction;
import com.apogee.common.mapper.interfaces.ThrowingFunction;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for object mapping operations, including collections and single objects.
 * Provides methods for transforming data with optional customization callbacks.
 */
public final class ObjectMapper {

    private static final Logger logger = LoggerFactory.getLogger(ObjectMapper.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper JSON_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();  // Cached for performance

    private ObjectMapper() {
        // Utility class
    }

    /**
     * Transform a collection of source objects to a list of destination objects using the provided mapping function.
     *
     * @param sourceCollection the collection of source objects to be transformed (nullable)
     * @param mappingFunction  the function that maps a source object to a destination object
     * @param <S>              the type of the source objects
     * @param <R>              the type of the destination objects
     * @return a list of transformed destination objects, or empty list if source is null/empty
     * @throws MapperException if any error occurs during the mapping process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, ThrowingFunction<S, R> mappingFunction) throws MapperException {
        return Optional.ofNullable(sourceCollection)
                .filter(collection -> !collection.isEmpty())
                .map(collection -> collection.stream()
                        .map(element -> applyMappingFunction(element, mappingFunction))
                        .toList())
                .orElse(new ArrayList<>());
    }

    /**
     * Transform a collection of source objects to a list of destination objects using the default ObjectMapper.
     *
     * @param sourceCollection the collection of source objects to be transformed (nullable)
     * @param destinationClass the class of the destination objects
     * @param <S>              the type of the source objects
     * @param <R>              the type of the destination objects
     * @return a list of transformed destination objects, or empty list if source is null/empty
     * @throws MapperException if any error occurs during the mapping process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, Class<R> destinationClass) throws MapperException {
        return transformCollection(sourceCollection, destinationClass, null);
    }

    /**
     * Transform a collection of source objects to a list of destination objects using the default ObjectMapper,
     * with an optional complementary function for post-mapping customization.
     *
     * @param sourceCollection      the collection of source objects to be transformed (nullable)
     * @param destinationClass      the class of the destination objects
     * @param complementaryFunction optional function to apply after mapping (nullable)
     * @param <S>                   the type of the source objects
     * @param <R>                   the type of the destination objects
     * @return a list of transformed destination objects, or empty list if source is null/empty
     * @throws MapperException if any error occurs during the mapping or function application process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, Class<R> destinationClass, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {
        return Optional.ofNullable(sourceCollection)
                .filter(collection -> !collection.isEmpty())
                .map(collection -> collection.stream()
                        .map(element -> applyMappingAndComplementary(element, destinationClass, complementaryFunction))
                        .toList())
                .orElse(new ArrayList<>());
    }

    /**
     * Transform a collection of source objects using a custom mapping function and an optional complementary function.
     *
     * @param sourceCollection      the collection of source objects to be transformed (nullable)
     * @param mappingFunction       the function that maps a source object to a destination object
     * @param complementaryFunction optional function to apply after mapping (nullable)
     * @param <S>                   the type of the source objects
     * @param <R>                   the type of the destination objects
     * @return a list of transformed destination objects, or empty list if source is null/empty
     * @throws MapperException if any error occurs during the mapping or function application process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, ThrowingFunction<S, R> mappingFunction, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {
        List<R> mappedList = transformCollection(sourceCollection, mappingFunction);
        if (complementaryFunction == null || mappedList.isEmpty()) {
            return mappedList;
        }

        List<R> result = new ArrayList<>();
        int index = 0;
        for (S source : sourceCollection) {
            R mapped = mappedList.get(index);
            result.add(applyComplementaryFunction(source, mapped, complementaryFunction));
            index++;
        }
        return result;
    }

    /**
     * Transform a single source object to a destination object using the default ObjectMapper,
     * with an optional complementary function for post-mapping customization.
     *
     * @param sourceObject          the source object to transform (nullable)
     * @param destinationClass      the class of the destination object
     * @param complementaryFunction optional function to apply after mapping (nullable)
     * @param <S>                   the type of the source object
     * @param <R>                   the type of the destination object
     * @return the transformed destination object, or null if source is null
     * @throws MapperException if any error occurs during the mapping or function application process
     */
    public static <S, R> R transform(S sourceObject, Class<R> destinationClass, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {
        if (sourceObject == null) {
            return null;
        }
        R mapped = mapObject(sourceObject, destinationClass);
        return complementaryFunction != null ? applyComplementaryFunction(sourceObject, mapped, complementaryFunction) : mapped;
    }

    /**
     * Transform a single source object to a destination object using the default ObjectMapper.
     *
     * @param sourceObject     the source object to transform (nullable)
     * @param destinationClass the class of the destination object
     * @param <S>              the type of the source object
     * @param <R>              the type of the destination object
     * @return the transformed destination object, or null if source is null
     * @throws MapperException if any error occurs during the mapping process
     */
    public static <S, R> R transform(S sourceObject, Class<R> destinationClass) throws MapperException {
        return transform(sourceObject, destinationClass, null);
    }

    /**
     * Format an object as a JSON string.
     *
     * @param object the object to format (nullable)
     * @return the JSON string representation, or null if object is null
     * @throws MapperException if JSON serialization fails
     */
    public static String formatAsJsonObject(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return JSON_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object to JSON", e);
            throw new MapperException("Failed to serialize object to JSON", e);
        }
    }

    // Helper methods

    private static <S, R> R applyMappingFunction(S element, ThrowingFunction<S, R> mappingFunction) throws MapperException {
        try {
            return mappingFunction.apply(element);
        } catch (Exception e) {
            logger.error("Error applying mapping function for element: {}", element, e);
            throw new MapperException("Mapping function failed: " + e.getMessage(), e);
        }
    }

    private static <S, R> R applyMappingAndComplementary(S element, Class<R> destinationClass, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {
        R mapped = mapObject(element, destinationClass);
        return complementaryFunction != null ? applyComplementaryFunction(element, mapped, complementaryFunction) : mapped;
    }

    private static <S, R> R applyComplementaryFunction(S source, R mapped, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {
        try {
            return complementaryFunction.apply(source, mapped);
        } catch (Exception e) {
            logger.error("Error applying complementary function for source: {} and mapped: {}", source, mapped, e);
            throw new MapperException("Complementary function failed: " + e.getMessage(), e);
        }
    }

    private static <S, R> R mapObject(S source, Class<R> destinationClass) throws MapperException {
        try {
            return getDefaultMapper().map(source, destinationClass);
        } catch (Exception e) {
            logger.error("Error mapping object: {} to class: {}", source, destinationClass, e);
            throw new MapperException("Object mapping failed: " + e.getMessage(), e);
        }
    }

    private static ObjectMappingEngine getDefaultMapper() {
        // In a real scenario, this could be injected or configurable
        return new ObjectMappingEngine();
    }
}

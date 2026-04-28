package com.apogee.common.mapper;

import com.apogee.common.exceptions.MapperException;
import com.apogee.common.mapper.interfaces.ThrowingBiFunction;
import com.apogee.common.mapper.interfaces.ThrowingFunction;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Deprecated
@Log4j2
public final class Utilities {

    private static final ObjectMappingEngine MAPPER =  ObjectMappingEngine.getInstance();

    private Utilities() {
    }

    /**
     * Transform a collection of source objects to a list of destination objects using the provided mapping function.
     *
     * @param sourceCollection the collection of source objects to be transformed
     * @param mappingFunction  a function that maps a source object to a destination object
     * @param <S>              the type of the source objects
     * @param <R>              the type of the destination objects
     * @return a list of transformed destination objects
     * @throws MapperException if any error occurs during the mapping process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, ThrowingFunction<S, R> mappingFunction) throws MapperException {

        if (sourceCollection != null && !sourceCollection.isEmpty()) {
            return sourceCollection.stream().map(element -> {
                try {
                    return mappingFunction.apply(element);
                } catch (Exception e) {
                    throw new MapperException(e.getMessage(), e);
                }
            }).toList();
        }
        return new ArrayList<>();
    }

    /**
     * Transform a collection of source objects to a list of destination objects using the Mapper utility.
     *
     * @param sourceCollection the collection of source objects to be transformed
     * @param destinationClass the class of the destination objects
     * @param <S>              the type of the source objects
     * @param <R>              the type of the destination objects
     * @return a list of transformed destination objects
     * @throws MapperException if any error occurs during the mapping process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, Class<R> destinationClass) throws MapperException {

        if (sourceCollection != null && !sourceCollection.isEmpty()) {
            return sourceCollection.stream().map(element -> {
                try {
                    return MAPPER.map(element, destinationClass);
                } catch (Exception e) {
                    throw new MapperException(e.getMessage(), e);
                }
            }).toList();
        }
        return new ArrayList<>();
    }


    /**
     * Transform a collection of source objects to a list of destination objects using the Mapper utility,
     * and apply a complementary function to each mapped object.
     *
     * @param sourceCollection      the collection of source objects to be transformed
     * @param destinationClass      the class of the destination objects
     * @param complementaryFunction a function that takes a source object and its corresponding mapped destination object,
     * @param <S>                   the type of the source objects
     * @param <R>                   the type of the destination objects
     * @return a list of transformed destination objects after applying the complementary function
     * @throws MapperException if any error occurs during the mapping or function application process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, Class<R> destinationClass, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {

        if (sourceCollection != null && !sourceCollection.isEmpty()) {
            return sourceCollection.stream().map(element -> {
                try {
                    R mapped = MAPPER.map(element, destinationClass);

                    return complementaryFunction != null ? complementaryFunction.apply(element, mapped) : mapped;
                } catch (Exception e) {
                    throw new MapperException(e.getMessage(), e);
                }
            }).toList();
        }
        return new ArrayList<>();
    }

    /**
     * Transform a collection of source objects to a list of destination objects using the provided mapping function,
     *
     * @param sourceCollection      the collection of source objects to be transformed
     * @param mappingFunction       a function that maps a source object to a destination object
     * @param complementaryFunction a function that takes a source object and its corresponding mapped destination object,
     * @param <S>                   the type of the source objects
     * @param <R>                   the type of the destination objects
     * @return a list of transformed destination objects after applying the complementary function
     * @throws MapperException if any error occurs during the mapping or function application process
     */
    public static <S, R> List<R> transformCollection(Collection<S> sourceCollection, ThrowingFunction<S, R> mappingFunction, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {

        List<R> destinationCollection = transformCollection(sourceCollection, mappingFunction);

        if (!destinationCollection.isEmpty() && complementaryFunction != null) {
            List<R> result = new ArrayList<>();
            int i = 0;
            for (S sourceElement : sourceCollection) {
                R destinationElement = destinationCollection.get(i);
                try {
                    result.add(complementaryFunction.apply(sourceElement, destinationElement));
                } catch (Exception e) {
                    throw new MapperException(e.getMessage(), e);
                }
                i++;
            }
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    public static <S, R> R transform(S sourceObject, Class<R> destinationClass, ThrowingBiFunction<S, R, R> complementaryFunction) throws MapperException {

        if (sourceObject != null) {
            try {
                R mapped = MAPPER.map(sourceObject, destinationClass);

                return complementaryFunction != null ? complementaryFunction.apply(sourceObject, mapped) : mapped;
            } catch (Exception e) {
                throw new MapperException(e.getMessage(), e);
            }
        }
        return null;
    }

    public static <S, R> R transform(S sourceObject, Class<R> destinationClass) throws MapperException {

        if (sourceObject != null) {
            try {

                return MAPPER.map(sourceObject, destinationClass);
            } catch (Exception e) {
                throw new MapperException(e.getMessage(), e);
            }
        }
        return null;
    }
}

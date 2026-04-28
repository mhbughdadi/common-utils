package com.apogee.common.mapper;

import com.apogee.common.exceptions.MapperException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class ObjectMappingEngine {

    private static final Set<Class<?>> SIMPLE_TYPES = Set.of(
            String.class, UUID.class, BigDecimal.class, BigInteger.class
    );
    private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Enum<?>[]> ENUM_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROP_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    // For performance, add field cache
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private ObjectMappingEngine () {
        //intentionally left blank to prevent instantiation
    }
    public <S, D> D map(S source, Class<D> destinationClass) throws Exception {

        if (source == null) {
            return null;
        }

        if (source.getClass().isEnum()) {
            return mapEnum(source, destinationClass);
        }

        D destinationObj = getNewDestinationInstance(destinationClass);

        // Use bean property descriptors (getters/setters) to avoid accessibility bypass.
        Map<String, PropertyDescriptor> sourceProps = getPropertyDescriptorMap(source.getClass());
        Map<String, PropertyDescriptor> destinationPropertyDescriptors = getPropertyDescriptorMap(destinationClass);

        // iterate over source properties
        for (Map.Entry<String, PropertyDescriptor> entry : sourceProps.entrySet()) {
            processProperty(source, destinationClass, entry, destinationPropertyDescriptors, destinationObj);
        }

        return destinationObj;
    }

    private <S, D> void processProperty(S source, Class<D> destinationClass, Map.Entry<String, PropertyDescriptor> entry, Map<String, PropertyDescriptor> destinationPropertyDescriptors, D destinationObj) throws Exception {
        String propName = entry.getKey();
        PropertyDescriptor sourcePd = entry.getValue();

        if (sourcePd.getReadMethod() == null) {
            return;
        }

        Object sourceValue = sourcePd.getReadMethod().invoke(source);

        PropertyDescriptor destinationPropertyDescriptor = destinationPropertyDescriptors.get(propName);
        if (destinationPropertyDescriptor == null || destinationPropertyDescriptor.getWriteMethod() == null) {
            return;
        }

        handleProperty(destinationClass, destinationObj, propName, sourcePd, sourceValue, destinationPropertyDescriptor);
    }

    private <D> void handleProperty(Class<D> destinationClass, D destinationObj, String propName, PropertyDescriptor sourcePd, Object sourceValue, PropertyDescriptor destinationPropertyDescriptor) throws Exception {

        Field destField = findField(destinationClass, propName);

        if (destField == null) {
            return; // skip if no corresponding field found in destination class
        }

        Class<?> sourceType = sourcePd.getPropertyType();

        if (sourceValue == null) {
            handleNullValue(destinationObj, destinationPropertyDescriptor);
            return;
        }

        if (isSimpleField(sourceType)) {
            destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, sourceValue);
        } else if (Collection.class.isAssignableFrom(sourceType)) {

            handleCollectionValue(destinationObj, sourceValue, destinationPropertyDescriptor, destField);
        } else if (Map.class.isAssignableFrom(sourceType)) {

            handleMapValue(destinationObj, sourceValue, destinationPropertyDescriptor, destField);
        } else {

            handleNestedObjectValue(destinationObj, sourceValue, destinationPropertyDescriptor, destField);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        return null;
    }

    private <D> void handleNestedObjectValue(D destinationObj, Object sourceValue, PropertyDescriptor destinationPropertyDescriptor, Field destField) throws Exception {
        Class<?> nestedDestClass = destField.getType();
        Object mapped = this.map(sourceValue, nestedDestClass);
        destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, mapped);
    }

    private <D> void handleMapValue(D destinationObj, Object sourceValue, PropertyDescriptor destinationPropertyDescriptor, Field destField) throws Exception {
        Object mappedMap = mapMap(sourceValue, destField);
        destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, mappedMap);
    }

    private <D> void handleCollectionValue(D destinationObj, Object sourceValue, PropertyDescriptor destinationPropertyDescriptor, Field destField) throws Exception {
        Object mappedCollection = mapCollection(sourceValue, destField);
        destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, mappedCollection);
    }

    private <D> void handleNullValue(D destinationObj, PropertyDescriptor destinationPropertyDescriptor) throws IllegalAccessException, InvocationTargetException {
        Class<?> destinationPropType = destinationPropertyDescriptor.getPropertyType();
        if (Collection.class.isAssignableFrom(destinationPropType)) {
            destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, createCollectionInstance(destinationPropType));
        } else if (Map.class.isAssignableFrom(destinationPropType)) {
            destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, createMapInstance(destinationPropType));
        } else {
            destinationPropertyDescriptor.getWriteMethod().invoke(destinationObj, (Object) null);
        }
    }

    private <S, D> D mapEnum(S source, Class<D> destinationClass) {

        Enum<?> sourceEnum = (Enum<?>) source;

        if (destinationClass.isEnum()) {

            Enum<?>[] destinationConstants = ENUM_CACHE.computeIfAbsent(destinationClass, aClass -> (Enum<?>[]) aClass.getEnumConstants());

            for (Enum<?> constant : destinationConstants) {
                if (constant.name().equals(sourceEnum.name())) {
                    return (D) constant;
                }
            }

            throw new MapperException(
                    "No matching enum constant for " + sourceEnum.name() +
                            " in " + destinationClass.getName()
            );
        } else if (destinationClass == String.class) {
            // Allow Enum -> String
            return (D) sourceEnum.name();
        } else if (destinationClass == int.class || destinationClass == Integer.class) {
            // Allow Enum -> ordinal
            return (D) Integer.valueOf(sourceEnum.ordinal());
        } else {
            throw new MapperException(
                    "Cannot map enum type " + source.getClass().getName() +
                            " to non-enum type " + destinationClass.getName()
            );
        }
    }

    private Collection<Object> mapCollection(Object sourceValue, Field destinationField) throws Exception {

        Collection<Object> destinationCollection = createCollectionInstance(destinationField.getType());

        if (sourceValue == null) {
            return destinationCollection;
        }

        Collection<?> sourceValueCollection = (Collection<?>) sourceValue;

        Class<?> genericType = getGenericType(destinationField);

        for (Object item : sourceValueCollection) {

            if (item == null) {
                destinationCollection.add(null);
            } else if (isSimpleField(item.getClass())) {
                destinationCollection.add(item);
            } else {
                destinationCollection.add(this.map(item, genericType));
            }
        }

        return destinationCollection;

    }

    private <D> D getNewDestinationInstance(Class<D> destinationClass) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        // getting reference to the destination class constructor from cache or creating a new one.
        @SuppressWarnings("unchecked")
        Constructor<D> constructor = (Constructor<D>) CONSTRUCTOR_CACHE.computeIfAbsent(destinationClass, cls -> {
            try {
                // prefer public no-arg constructor
                return cls.getDeclaredConstructor();
            } catch (Exception e) {
                throw new MapperException("No default constructor found for class: " + cls.getName(), e);
            }
        });

        // creating an instance from the destination class constructor.
        return constructor.newInstance();
    }

    private Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> cls) {
        return PROP_DESCRIPTOR_CACHE.computeIfAbsent(cls, c -> {
            Map<String, PropertyDescriptor> map = new HashMap<>();
            try {
                map = Arrays.stream(Introspector.getBeanInfo(c).getPropertyDescriptors()).filter(propertyDescriptor -> !"class".equals(propertyDescriptor.getName())).collect(Collectors.toMap(PropertyDescriptor::getName, pd -> pd));
            } catch (IntrospectionException e) {
                throw new MapperException("Failed to introspect class: " + c.getName(), e);
            }
            return map;
        });
    }

    private Object mapMap(Object sourceValue, Field destinationField) throws Exception {
        if (sourceValue == null) {
            return null;
        }

        Map<?, ?> sourceMap = (Map<?, ?>) sourceValue;
        Map<Object, Object> destinationMap = createMapInstance(destinationField.getType());

        Class<?>[] genericTypes = getGenericTypes(destinationField);

        for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            Object mappedKey = (isSimpleField(key.getClass())) ? key : this.map(key, genericTypes[0]);
            Object mappedValue = (isSimpleField(value.getClass())) ? value : this.map(value, genericTypes[1]);

            destinationMap.put(mappedKey, mappedValue);
        }

        return destinationMap;
    }

    private Class<?>[] getGenericTypes(Field targetField) throws ClassNotFoundException {

        Type type = targetField.getGenericType();

        if (type instanceof ParameterizedType parameterizedType) {

            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            Class<?> keyType = Class.forName(actualTypes[0].getTypeName());
            Class<?> valueType = Class.forName(actualTypes[1].getTypeName());
            return new Class<?>[]{keyType, valueType};
        }
        return new Class<?>[]{Object.class, Object.class};
    }

    private Class<?> getGenericType(Field targetField) throws ClassNotFoundException {

        Type type = targetField.getGenericType();

        if (type instanceof ParameterizedType parameterizedType) {

            Type actual = parameterizedType.getActualTypeArguments()[0];

            return Class.forName(actual.getTypeName());
        }

        return Object.class;
    }


    private Collection<Object> createCollectionInstance(Class<?> type) {

        if (List.class.isAssignableFrom(type)) {

            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(type)) {

            return new HashSet<>();
        } else {

            throw new MapperException("Not Supported Collection Type: " + type.getName());
        }
    }

    private Map<Object, Object> createMapInstance(Class<?> type) {

        if (Map.class.isAssignableFrom(type)) {
            return new HashMap<>();
        } else {
            throw new MapperException("Not Supported Map Type: " + type.getName());
        }
    }


    private boolean isSimpleField(Class<?> sourceClass) {

        return sourceClass.isPrimitive()
                || SIMPLE_TYPES.contains(sourceClass)
                || Number.class.isAssignableFrom(sourceClass)
                || Character.class.isAssignableFrom(sourceClass)
                || Boolean.class.isAssignableFrom(sourceClass)
                || Date.class.isAssignableFrom(sourceClass)
                || java.time.temporal.Temporal.class.isAssignableFrom(sourceClass);
    }

    private static class Holder {
        private static final ObjectMappingEngine INSTANCE =
                new ObjectMappingEngine();
    }

    public static ObjectMappingEngine getInstance() {
        return Holder.INSTANCE;
    }

}

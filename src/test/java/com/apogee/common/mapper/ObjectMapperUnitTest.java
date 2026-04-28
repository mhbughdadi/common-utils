package com.apogee.common.mapper;

import com.apogee.common.exceptions.MapperException;
import com.apogee.common.mapper.interfaces.ThrowingBiFunction;
import com.apogee.common.mapper.interfaces.ThrowingFunction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperUnitTest {

    static class A {
        private String name;
        private int value;

        public A() {}

        public A(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    static class B {
        private String name;
        private int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    @Test
    void transformCollection_withMappingFunction() {
        List<A> input = List.of(new A("x", 1), new A("y", 2));
        List<B> out = ObjectMapper.transformCollection(input, (ThrowingFunction<A, B>) a -> {
            B b = new B();
            b.setName(a.getName());
            b.setValue(a.getValue());
            return b;
        });

        assertNotNull(out);
        assertEquals(2, out.size());
        assertEquals("x", out.get(0).getName());
        assertEquals(2, out.get(1).getValue());
    }

    @Test
    void transformCollection_withClassMapping() {
        List<A> input = List.of(new A("alpha", 10));
        List<B> out = ObjectMapper.transformCollection(input, B.class);
        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals("alpha", out.get(0).getName());
        assertEquals(10, out.get(0).getValue());
    }

    @Test
    void transformCollection_withComplementaryFunction() {
        List<A> input = List.of(new A("one", 1), new A("two", 2));
        List<B> out = ObjectMapper.transformCollection(input, B.class, (ThrowingBiFunction<A, B, B>) (src, dst) -> {
            dst.setName(dst.getName() + "-mapped");
            dst.setValue(dst.getValue() + 100);
            return dst;
        });

        assertEquals("one-mapped", out.get(0).getName());
        assertEquals(101, out.get(0).getValue());
    }

    @Test
    void transformCollection_withMappingAndComplementaryFunction_variant() {
        List<A> input = List.of(new A("a", 1));
        List<B> out = ObjectMapper.transformCollection(input,
                (ThrowingFunction<A, B>) a -> {
                    B b = new B();
                    b.setName("pref-" + a.getName());
                    b.setValue(a.getValue());
                    return b;
                },
                (ThrowingBiFunction<A, B, B>) (src, dst) -> {
                    dst.setName(dst.getName() + "-post");
                    return dst;
                });

        assertEquals(1, out.size());
        assertEquals("pref-a-post", out.get(0).getName());
    }

    @Test
    void transform_single_null_returnsNull() {
        B b = ObjectMapper.transform(null, B.class);
        assertNull(b);
    }

    @Test
    void transform_single_withComplementaryFunction() {
        A a = new A("Z", 99);
        B b = ObjectMapper.transform(a, B.class, (ThrowingBiFunction<A, B, B>) (src, dst) -> {
            dst.setName(dst.getName() + "-c");
            return dst;
        });
        assertEquals("Z-c", b.getName());
    }

    @Test
    void formatAsJsonObject_null() {
        assertNull(ObjectMapper.formatAsJsonObject(null));
    }

    @Test
    void formatAsJsonObject_ok() {
        A a = new A("json", 5);
        String json = ObjectMapper.formatAsJsonObject(a);
        assertNotNull(json);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"value\""));
    }

    @Test
    void applyMappingFunction_throwsWrappedMapperException() {
        List<String> input = List.of("x", "y");
        assertThrows(MapperException.class, () -> ObjectMapper.transformCollection(input, (ThrowingFunction<String, B>) s -> {
            throw new Exception("boom");
        }));
    }

    @Test
    void applyComplementaryFunction_throwsWrappedMapperException() {
        List<A> input = List.of(new A("n", 1));
        assertThrows(MapperException.class, () -> ObjectMapper.transformCollection(input, B.class, (ThrowingBiFunction<A, B, B>) (src, dst) -> {
            throw new Exception("comp-ex");
        }));
    }
}


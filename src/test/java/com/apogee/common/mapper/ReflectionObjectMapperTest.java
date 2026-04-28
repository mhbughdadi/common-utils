package com.apogee.common.mapper;

import com.apogee.common.exceptions.MapperException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionObjectMapperTest {

    private final ObjectMappingEngine mapper = new ObjectMappingEngine();

    static class Source {
        private String name;
        private int age;
        private List<String> hobbies;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<String> getHobbies() { return hobbies; }
        public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
    }

    static class Destination {
        private String name;
        private int age;
        private List<String> hobbies;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public List<String> getHobbies() { return hobbies; }
        public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
    }

    @Test
    void testMapSimpleObject() throws Exception {
        Source source = new Source();
        source.setName("John");
        source.setAge(30);

        Destination dest = mapper.map(source, Destination.class);

        assertNotNull(dest);
        assertEquals("John", dest.getName());
        assertEquals(30, dest.getAge());
    }

    @Test
    void testMapWithCollection() throws Exception {
        Source source = new Source();
        source.setHobbies(List.of("Reading", "Swimming"));

        Destination dest = mapper.map(source, Destination.class);

        assertNotNull(dest);
        assertEquals(List.of("Reading", "Swimming"), dest.getHobbies());
    }

    @Test
    void testMapNull() throws Exception {
        Destination dest = mapper.map(null, Destination.class);
        assertNull(dest);
    }

    @Test
    void testMapEnumToString() throws Exception {
        TestEnum source = TestEnum.VALUE1;
        String dest = mapper.map(source, String.class);
        assertEquals("VALUE1", dest);
    }

    @Test
    void testMapInvalidEnum() {
        assertThrows(MapperException.class, () -> mapper.map(TestEnum.VALUE1, Double.class));
    }

    enum TestEnum {
        VALUE1, VALUE2
    }
}

package com.apogee.common.mapper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMappingEngineMapNullKeyValueTest {

    static class Dest {
        private Map<String, Integer> map;

        public Map<String, Integer> getMap() { return map; }
        public void setMap(Map<String, Integer> map) { this.map = map; }
    }

    @Test
    void mapMap_handlesNullKeyAndValue() throws Exception {
        ObjectMappingEngine engine = ObjectMappingEngine.getInstance();

        // create source with null key and null value
        Map<String, Integer> srcMap = new HashMap<>();
        srcMap.put(null, null);
        // wrap source object with a property so engine will process it
        class Wrapper { private Map<String,Integer> map; public Map<String,Integer> getMap(){return map;} public void setMap(Map<String,Integer> m){this.map=m;} }
        Wrapper wrapper = new Wrapper();
        wrapper.setMap(srcMap);

        Dest out = engine.map(wrapper, Dest.class);
        assertNotNull(out);
        assertNotNull(out.getMap());
        assertTrue(out.getMap().containsKey(null));
        assertNull(out.getMap().get(null));
    }
}


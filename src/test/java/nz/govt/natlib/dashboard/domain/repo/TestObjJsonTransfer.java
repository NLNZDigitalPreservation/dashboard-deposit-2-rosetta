package nz.govt.natlib.dashboard.domain.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class TestObjJsonTransfer {
    public Object json2Object(String json, Class<?> clazz) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    @Test
    public void json2ObjectWithExtraField() {
//        String json1 = "{\"x\":\"a\",\"y\":\"b\"}";
        String json1 = "{\"x\":\"a\",\"y\":\"b\",\"z\":\"c\"}";
        try {
            DemoObject obj1 = (DemoObject) json2Object(json1, DemoObject.class);
        } catch (JsonProcessingException e) {
            assert true;
//            e.printStackTrace();
        }
    }

    @Test
    public void json2ObjectWithLackField() {
        String json1 = "{\"x\":\"a\"}";
        try {
            DemoObject obj1 = (DemoObject) json2Object(json1, DemoObject.class);
            assert true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            assert false;
            return;
        }
    }

    @Test
    public void json2Map() {
        String json1 = "{\"x\":\"a\",\"i\":9989, \"b\":true}";
        try {
            TypeReference<HashMap<String, Object>> typeRef
                    = new TypeReference<HashMap<String, Object>>() {
            };
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> map1 = objectMapper.readValue(json1, typeRef);
            assert true;
            System.out.println("x:" + map1.get("x"));
            System.out.println("i:" + map1.get("i"));
            System.out.println("b:" + map1.get("b"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            assert false;
        }
    }

    static class DemoObject {
        private String x;
        private String y;

        private int i;

        public String getX() {
            return x;
        }

        public void setX(String x) {
            this.x = x;
        }

        public String getY() {
            return y;
        }

        public void setY(String y) {
            this.y = y;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }
    }
}

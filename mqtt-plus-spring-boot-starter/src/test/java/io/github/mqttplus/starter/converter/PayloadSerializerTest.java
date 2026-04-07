package io.github.mqttplus.starter.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadSerializerTest {

    @Test
    void byteArraySerializerShouldSupportByteArray() {
        ByteArrayPayloadSerializer serializer = new ByteArrayPayloadSerializer();
        assertTrue(serializer.supports(byte[].class));
        assertFalse(serializer.supports(String.class));
        assertFalse(serializer.supports(Object.class));
    }

    @Test
    void byteArraySerializerShouldReturnSameBytes() {
        ByteArrayPayloadSerializer serializer = new ByteArrayPayloadSerializer();
        byte[] input = "hello".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(input, serializer.serialize(input));
    }

    @Test
    void stringSerializerShouldSupportString() {
        StringPayloadSerializer serializer = new StringPayloadSerializer();
        assertTrue(serializer.supports(String.class));
        assertFalse(serializer.supports(byte[].class));
        assertFalse(serializer.supports(Object.class));
    }

    @Test
    void stringSerializerShouldConvertToUtf8Bytes() {
        StringPayloadSerializer serializer = new StringPayloadSerializer();
        byte[] result = serializer.serialize("hello");
        assertEquals("hello", new String(result, StandardCharsets.UTF_8));
    }

    @Test
    void jacksonSerializerShouldSupportNonPrimitiveTypes() {
        JacksonPayloadSerializer serializer = new JacksonPayloadSerializer(new ObjectMapper());
        assertTrue(serializer.supports(Map.class));
        assertTrue(serializer.supports(Object.class));
        assertFalse(serializer.supports(byte[].class));
        assertFalse(serializer.supports(String.class));
    }

    @Test
    void jacksonSerializerShouldSerializeToJson() {
        JacksonPayloadSerializer serializer = new JacksonPayloadSerializer(new ObjectMapper());
        byte[] result = serializer.serialize(Map.of("action", "land"));
        String json = new String(result, StandardCharsets.UTF_8);
        assertTrue(json.contains("\"action\""));
        assertTrue(json.contains("\"land\""));
    }
}

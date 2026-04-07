package io.github.mqttplus.starter.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqttplus.core.converter.PayloadSerializer;

public final class JacksonPayloadSerializer implements PayloadSerializer {

    private final ObjectMapper objectMapper;

    public JacksonPayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(Class<?> sourceType) {
        return !byte[].class.equals(sourceType) && !String.class.equals(sourceType);
    }

    @Override
    public byte[] serialize(Object payload) {
        try {
            return objectMapper.writeValueAsBytes(payload);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize payload of type " + payload.getClass().getName(), ex);
        }
    }
}

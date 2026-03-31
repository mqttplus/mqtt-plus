package io.github.mqttplus.starter.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqttplus.core.converter.PayloadConverter;

public final class JacksonPayloadConverter implements PayloadConverter {

    private final ObjectMapper objectMapper;

    public JacksonPayloadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(Class<?> targetType) {
        return !(String.class.equals(targetType) || byte[].class.equals(targetType));
    }

    @Override
    public Object convert(byte[] payload, Class<?> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to convert payload to " + targetType.getName(), ex);
        }
    }
}

package io.github.mqttplus.starter.converter;

import io.github.mqttplus.core.converter.PayloadConverter;

public final class StringPayloadConverter implements PayloadConverter {

    @Override
    public boolean supports(Class<?> targetType) {
        return String.class.equals(targetType);
    }

    @Override
    public Object convert(byte[] payload, Class<?> targetType) {
        return new String(payload);
    }
}

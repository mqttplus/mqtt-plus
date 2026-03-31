package io.github.mqttplus.starter.converter;

import io.github.mqttplus.core.converter.PayloadConverter;

public final class ByteArrayPayloadConverter implements PayloadConverter {

    @Override
    public boolean supports(Class<?> targetType) {
        return byte[].class.equals(targetType);
    }

    @Override
    public Object convert(byte[] payload, Class<?> targetType) {
        return payload.clone();
    }
}

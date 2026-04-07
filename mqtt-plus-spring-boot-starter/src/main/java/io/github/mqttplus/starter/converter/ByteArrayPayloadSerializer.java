package io.github.mqttplus.starter.converter;

import io.github.mqttplus.core.converter.PayloadSerializer;

public final class ByteArrayPayloadSerializer implements PayloadSerializer {

    @Override
    public boolean supports(Class<?> sourceType) {
        return byte[].class.equals(sourceType);
    }

    @Override
    public byte[] serialize(Object payload) {
        return (byte[]) payload;
    }
}

package io.github.mqttplus.starter.converter;

import io.github.mqttplus.core.converter.PayloadSerializer;

import java.nio.charset.StandardCharsets;

public final class StringPayloadSerializer implements PayloadSerializer {

    @Override
    public boolean supports(Class<?> sourceType) {
        return String.class.equals(sourceType);
    }

    @Override
    public byte[] serialize(Object payload) {
        return ((String) payload).getBytes(StandardCharsets.UTF_8);
    }
}

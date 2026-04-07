package io.github.mqttplus.core.converter;

public interface PayloadSerializer {

    boolean supports(Class<?> sourceType);

    byte[] serialize(Object payload);
}

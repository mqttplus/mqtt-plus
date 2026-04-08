package io.github.mqttplus.spring.invocation;

import io.github.mqttplus.core.invocation.ListenerInvoker;
import io.github.mqttplus.core.model.MqttContext;
import io.github.mqttplus.core.model.MqttListenerDefinition;

import java.lang.reflect.InvocationTargetException;

public final class SpringMqttListenerInvoker implements ListenerInvoker {

    private final MqttListenerMethodArgumentResolver argumentResolver;

    public SpringMqttListenerInvoker(MqttListenerMethodArgumentResolver argumentResolver) {
        this.argumentResolver = argumentResolver;
    }

    @Override
    public void invoke(MqttListenerDefinition definition, Object payload, MqttContext context) throws Exception {
        try {
            definition.getMethod().setAccessible(true);
            Object[] arguments = argumentResolver.resolveArguments(
                    definition.getMethod(),
                    payload,
                    context.getPayload(),
                    context.getTopic(),
                    context.getHeaders());
            definition.getMethod().invoke(definition.getBean(), arguments);
        }
        catch (InvocationTargetException ex) {
            Throwable target = ex.getTargetException();
            if (target instanceof Exception exception) {
                throw exception;
            }
            throw ex;
        }
    }
}
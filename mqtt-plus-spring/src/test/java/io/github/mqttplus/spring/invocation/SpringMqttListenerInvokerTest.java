package io.github.mqttplus.spring.invocation;

import io.github.mqttplus.core.annotation.MqttTopic;
import io.github.mqttplus.core.model.MqttContext;
import io.github.mqttplus.core.model.MqttHeaders;
import io.github.mqttplus.core.model.MqttListenerDefinition;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringMqttListenerInvokerTest {

    @Test
    void shouldInvokeListenerWithResolvedArguments() throws Exception {
        SampleHandler handler = new SampleHandler();
        Method method = SampleHandler.class.getDeclaredMethod("handle", String.class, MqttHeaders.class, String.class, byte[].class);
        MqttListenerDefinition definition = new MqttListenerDefinition(
                "sampleHandler",
                handler,
                method,
                "cloud",
                List.of("drone/+/status"),
                1,
                String.class);
        MqttHeaders headers = new MqttHeaders(Map.of("qos", 1));
        byte[] rawPayload = "payload".getBytes();
        MqttContext context = new MqttContext("cloud", "drone/DRONE999/status", rawPayload, headers);

        new SpringMqttListenerInvoker(new MqttListenerMethodArgumentResolver())
                .invoke(definition, "payload", context);

        assertEquals("payload", handler.payload);
        assertEquals(headers, handler.headers);
        assertEquals("drone/DRONE999/status", handler.topic);
        assertArrayEquals(rawPayload, handler.rawPayload);
    }

    static final class SampleHandler {
        private String payload;
        private MqttHeaders headers;
        private String topic;
        private byte[] rawPayload;

        void handle(String payload, MqttHeaders headers, @MqttTopic String topic, byte[] rawPayload) {
            this.payload = payload;
            this.headers = headers;
            this.topic = topic;
            this.rawPayload = rawPayload;
        }
    }
}
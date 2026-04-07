package io.github.mqttplus.core;

import io.github.mqttplus.core.adapter.DefaultMqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.converter.PayloadSerializer;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMqttTemplateTest {

    @Test
    void shouldDelegatePublishToResolvedAdapter() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);
        DefaultMqttTemplate template = new DefaultMqttTemplate(registry);

        template.publish("primary", "devices/1/status", "ok");

        assertEquals("devices/1/status", adapter.lastTopic);
        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("ok", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
        assertEquals(0, adapter.lastQos);
        assertFalse(adapter.lastRetained);
    }

    @Test
    void shouldDelegatePublishOptionsToResolvedAdapter() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);
        DefaultMqttTemplate template = new DefaultMqttTemplate(registry);

        template.publish("primary", "devices/1/status", "ok", 1, true);

        assertEquals("devices/1/status", adapter.lastTopic);
        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("ok", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
        assertEquals(1, adapter.lastQos);
        assertTrue(adapter.lastRetained);
    }

    @Test
    void shouldFailWhenBrokerIsMissing() {
        DefaultMqttTemplate template = new DefaultMqttTemplate(new DefaultMqttClientAdapterRegistry());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> template.publish("missing", "topic", "payload"));

        assertEquals("No adapter registered for broker: missing", exception.getMessage());
    }

    @Test
    void shouldSerializePayloadThroughSerializerChain() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);

        PayloadSerializer jsonSerializer = new PayloadSerializer() {
            @Override
            public boolean supports(Class<?> sourceType) {
                return !byte[].class.equals(sourceType) && !String.class.equals(sourceType);
            }

            @Override
            public byte[] serialize(Object payload) {
                return "{\"mock\":true}".getBytes(StandardCharsets.UTF_8);
            }
        };

        DefaultMqttTemplate template = new DefaultMqttTemplate(registry, List.of(jsonSerializer));
        template.publish("primary", "test/topic", new Object());

        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("{\"mock\":true}", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
    }

    @Test
    void shouldPassByteArrayPayloadDirectly() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);

        PayloadSerializer neverCalled = new PayloadSerializer() {
            @Override
            public boolean supports(Class<?> sourceType) {
                return true;
            }

            @Override
            public byte[] serialize(Object payload) {
                throw new AssertionError("should not be called");
            }
        };

        DefaultMqttTemplate template = new DefaultMqttTemplate(registry, List.of(neverCalled));
        byte[] raw = "raw-bytes".getBytes(StandardCharsets.UTF_8);
        template.publish("primary", "test/topic", raw);

        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertArrayEquals(raw, (byte[]) adapter.lastPayload);
    }

    @Test
    void shouldConvertStringPayloadToBytes() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);

        DefaultMqttTemplate template = new DefaultMqttTemplate(registry, List.of());
        template.publish("primary", "test/topic", "hello");

        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("hello", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
    }

    @Test
    void shouldFallbackToStringValueOfWhenNoSerializerMatches() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);

        DefaultMqttTemplate template = new DefaultMqttTemplate(registry, List.of());
        template.publish("primary", "test/topic", 42);

        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("42", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
    }

    @Test
    void shouldSerializeNullPayloadAsLiteralNull() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);

        DefaultMqttTemplate template = new DefaultMqttTemplate(registry, List.of());
        template.publish("primary", "test/topic", null);

        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("null", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
    }

    @Test
    void shouldSerializePayloadForAsyncPublish() throws Exception {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        registry.register(adapter);

        PayloadSerializer jsonSerializer = new PayloadSerializer() {
            @Override
            public boolean supports(Class<?> sourceType) {
                return !byte[].class.equals(sourceType) && !String.class.equals(sourceType);
            }

            @Override
            public byte[] serialize(Object payload) {
                return "{\"async\":true}".getBytes(StandardCharsets.UTF_8);
            }
        };

        DefaultMqttTemplate template = new DefaultMqttTemplate(registry, List.of(jsonSerializer));
        template.publishAsync("primary", "test/topic", new Object(), 1, true).get();

        assertInstanceOf(byte[].class, adapter.lastPayload);
        assertEquals("{\"async\":true}", new String((byte[]) adapter.lastPayload, StandardCharsets.UTF_8));
        assertEquals(1, adapter.lastQos);
        assertTrue(adapter.lastRetained);
    }

    static final class RecordingAdapter implements MqttClientAdapter {
        private final MqttBrokerDefinition definition;
        private String lastTopic;
        private Object lastPayload;
        private int lastQos;
        private boolean lastRetained;

        RecordingAdapter(String brokerId) {
            this.definition = MqttBrokerDefinition.builder()
                    .brokerId(brokerId)
                    .host("127.0.0.1")
                    .clientId("template-test")
                    .inboundThreadPool(ThreadPoolConfig.builder().build())
                    .build();
        }

        @Override
        public String getBrokerId() {
            return definition.getBrokerId();
        }

        @Override
        public MqttBrokerDefinition getBrokerDefinition() {
            return definition;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void subscribe(String topic, int qos) {
        }

        @Override
        public void unsubscribe(String topic) {
        }

        @Override
        public void publish(String topic, Object payload) {
            publish(topic, payload, 0, false);
        }

        @Override
        public void publish(String topic, Object payload, int qos, boolean retained) {
            this.lastTopic = topic;
            this.lastPayload = payload;
            this.lastQos = qos;
            this.lastRetained = retained;
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, Object payload) {
            return publishAsync(topic, payload, 0, false);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, Object payload, int qos, boolean retained) {
            publish(topic, payload, qos, retained);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean supportsManualAck() {
            return false;
        }

        @Override
        public void addConnectionListener(MqttConnectionListener listener) {
        }
    }
}

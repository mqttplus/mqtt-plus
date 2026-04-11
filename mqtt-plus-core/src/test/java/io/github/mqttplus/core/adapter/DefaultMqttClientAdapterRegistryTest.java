package io.github.mqttplus.core.adapter;

import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMqttClientAdapterRegistryTest {

    @Test
    void shouldRegisterFindAndRemoveAdapters() {
        DefaultMqttClientAdapterRegistry registry = new DefaultMqttClientAdapterRegistry();
        MqttClientAdapter adapter = new TestAdapter("primary");

        registry.register(adapter);

        assertTrue(registry.find("primary").isPresent());
        assertFalse(registry.find("missing").isPresent());

        registry.remove("primary");

        assertFalse(registry.find("primary").isPresent());
    }

    private static final class TestAdapter implements MqttClientAdapter {
        private final MqttBrokerDefinition definition;

        private TestAdapter(String brokerId) {
            this.definition = MqttBrokerDefinition.builder()
                    .brokerId(brokerId)
                    .host("127.0.0.1")
                    .clientId("test-client")
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
        public void publish(String topic, byte[] payload) {
            publish(topic, payload, 0, false);
        }

        @Override
        public void publish(String topic, byte[] payload, int qos, boolean retained) {
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload) {
            return publishAsync(topic, payload, 0, false);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, Executor executor) {
            return publishAsync(topic, payload, 0, false, executor);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained,
                Executor executor) {
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
package io.github.mqttplus.samples.dynamicsubscription;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SpringBootTest(properties = "mqtt-plus.brokers.primary.adapter=paho")
@Import(SampleDynamicSubscriptionApplicationTest.TestConfig.class)
class SampleDynamicSubscriptionApplicationTest {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean(name = "pahoMqttClientAdapterFactory")
        MqttClientAdapterFactory pahoMqttClientAdapterFactory() {
            return new StubFactory();
        }
    }

    private static final class StubFactory implements MqttClientAdapterFactory {
        @Override
        public String adapterId() {
            return "paho";
        }

        @Override
        public boolean supportsMqttVersion(String mqttVersion) {
            return "3.1.1".equals(mqttVersion);
        }

        @Override
        public MqttClientAdapter create(MqttBrokerDefinition brokerDefinition, MqttInboundMessageSink inboundMessageSink) {
            return new StubAdapter(brokerDefinition);
        }
    }

    private static final class StubAdapter implements MqttClientAdapter {
        private final MqttBrokerDefinition brokerDefinition;

        private StubAdapter(MqttBrokerDefinition brokerDefinition) {
            this.brokerDefinition = brokerDefinition;
        }

        @Override
        public String getBrokerId() {
            return brokerDefinition.getBrokerId();
        }

        @Override
        public MqttBrokerDefinition getBrokerDefinition() {
            return brokerDefinition;
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
        }

        @Override
        public void publish(String topic, byte[] payload, int qos, boolean retained) {
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload) {
            return CompletableFuture.completedFuture(null);
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
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained, Executor executor) {
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
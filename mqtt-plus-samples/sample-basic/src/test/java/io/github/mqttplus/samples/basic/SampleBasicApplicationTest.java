package io.github.mqttplus.samples.basic;

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

@SpringBootTest(properties = "samples.publish-on-startup=false")
@Import(SampleBasicApplicationTest.TestConfig.class)
class SampleBasicApplicationTest {

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
        public String supportedVersion() {
            return "3.1.1";
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
        public void publish(String topic, Object payload) {
        }

        @Override
        public void publish(String topic, Object payload, int qos, boolean retained) {
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, Object payload) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, Object payload, int qos, boolean retained) {
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
package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MqttClientAdapterFactoryRegistryTest {

    @Test
    void shouldReturnFactoryForKnownAdapterId() {
        MqttClientAdapterFactory factory = new StubFactory("paho", "3.1.1");
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of(factory));

        assertEquals(factory, registry.getRequiredByAdapterId("paho"));
    }

    @Test
    void shouldResolveFactoryByExplicitAdapterId() {
        MqttClientAdapterFactory factory = new StubFactory("spring-integration", "3.1.1");
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of(factory));

        assertEquals(factory, registry.resolveFactory("spring-integration", "3.1.1"));
    }

    @Test
    void shouldPreferSpringIntegrationForCompatibleVersion() {
        MqttClientAdapterFactory paho = new StubFactory("paho", "3.1.1");
        MqttClientAdapterFactory integration = new StubFactory("spring-integration", "3.1.1");
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of(paho, integration));

        assertEquals(integration, registry.resolveFactory(null, "3.1.1"));
    }

    @Test
    void shouldFailWhenFactoryIsMissingForAdapterId() {
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> registry.getRequiredByAdapterId("spring-integration"));

        assertEquals("No MQTT adapter factory registered for adapter: spring-integration", exception.getMessage());
    }

    @Test
    void shouldFailWhenNoCompatibleFactoryExists() {
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of(new StubFactory("paho", "3.1.1")));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> registry.resolveFactory(null, "5.0"));

        assertEquals("No MQTT adapter factory registered for version: 5.0", exception.getMessage());
    }

    private static final class StubFactory implements MqttClientAdapterFactory {
        private final String adapterId;
        private final String version;

        private StubFactory(String adapterId, String version) {
            this.adapterId = adapterId;
            this.version = version;
        }

        @Override
        public String adapterId() {
            return adapterId;
        }

        @Override
        public boolean supportsMqttVersion(String mqttVersion) {
            return version.equals(mqttVersion);
        }

        @Override
        public MqttClientAdapter create(MqttBrokerDefinition brokerDefinition, MqttInboundMessageSink inboundMessageSink) {
            throw new UnsupportedOperationException();
        }
    }
}
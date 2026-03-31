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
    void shouldReturnFactoryForKnownVersion() {
        MqttClientAdapterFactory factory = new StubFactory("3.1.1");
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of(factory));

        assertEquals(factory, registry.getRequiredFactory("3.1.1"));
    }

    @Test
    void shouldFailWhenFactoryIsMissing() {
        MqttClientAdapterFactoryRegistry registry = new MqttClientAdapterFactoryRegistry(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> registry.getRequiredFactory("5.0"));

        assertEquals("No MQTT adapter factory registered for version: 5.0", exception.getMessage());
    }

    private static final class StubFactory implements MqttClientAdapterFactory {
        private final String version;

        private StubFactory(String version) {
            this.version = version;
        }

        @Override
        public String supportedVersion() {
            return version;
        }

        @Override
        public MqttClientAdapter create(MqttBrokerDefinition brokerDefinition, MqttInboundMessageSink inboundMessageSink) {
            throw new UnsupportedOperationException();
        }
    }
}

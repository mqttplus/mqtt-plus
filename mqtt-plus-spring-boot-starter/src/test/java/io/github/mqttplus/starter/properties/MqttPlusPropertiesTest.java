package io.github.mqttplus.starter.properties;

import io.github.mqttplus.core.model.MqttBrokerDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MqttPlusPropertiesTest {

    @Test
    void shouldApplyBrokerDefaults() {
        MqttPlusProperties.BrokerProperties properties = new MqttPlusProperties.BrokerProperties();
        properties.setHost("127.0.0.1");
        properties.setClientId("demo-client");

        MqttBrokerDefinition definition = properties.toDefinition("primary");

        assertEquals("3.1.1", properties.getMqttVersion());
        assertEquals(1883, definition.getPort());
        assertEquals(60, definition.getKeepAliveInterval());
        assertEquals(30, definition.getConnectionTimeout());
        assertEquals(2, definition.getInboundThreadPool().getCoreSize());
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        MqttPlusProperties.BrokerProperties properties = new MqttPlusProperties.BrokerProperties();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> properties.toDefinition("primary"));

        assertEquals("host must not be blank", exception.getMessage());
    }
}

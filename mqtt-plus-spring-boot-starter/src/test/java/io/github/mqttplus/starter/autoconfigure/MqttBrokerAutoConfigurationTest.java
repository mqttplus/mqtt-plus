package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.adapter.DefaultMqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MqttBrokerAutoConfigurationTest {

    @Test
    void shouldConnectAdapterAfterRegistering() {
        MqttPlusProperties properties = createProperties();
        StubAdapter adapter = new StubAdapter(properties.getBrokers().get("primary").toDefinition("primary"), false);
        MqttClientAdapterFactoryRegistry factoryRegistry = new MqttClientAdapterFactoryRegistry(List.of(new StubFactory(adapter)));
        DefaultMqttClientAdapterRegistry adapterRegistry = new DefaultMqttClientAdapterRegistry();

        new MqttBrokerAutoConfiguration().registerAdapters(
                properties,
                factoryRegistry,
                adapterRegistry,
                (brokerId, topic, payload, headers) -> { },
                List.of(new NoOpConnectionListener()));

        assertTrue(adapter.connected);
        assertTrue(adapterRegistry.find("primary").isPresent());
    }

    @Test
    void shouldRegisterAllConnectionListeners() {
        MqttPlusProperties properties = createProperties();
        StubAdapter adapter = new StubAdapter(properties.getBrokers().get("primary").toDefinition("primary"), false);
        MqttClientAdapterFactoryRegistry factoryRegistry = new MqttClientAdapterFactoryRegistry(List.of(new StubFactory(adapter)));
        DefaultMqttClientAdapterRegistry adapterRegistry = new DefaultMqttClientAdapterRegistry();
        List<MqttConnectionListener> listeners = List.of(new NoOpConnectionListener(), new NoOpConnectionListener());

        new MqttBrokerAutoConfiguration().registerAdapters(
                properties,
                factoryRegistry,
                adapterRegistry,
                (brokerId, topic, payload, headers) -> { },
                listeners);

        assertEquals(2, adapter.connectionListeners.size());
    }

    @Test
    void shouldRemoveAdapterWhenConnectFails() {
        MqttPlusProperties properties = createProperties();
        StubAdapter adapter = new StubAdapter(properties.getBrokers().get("primary").toDefinition("primary"), true);
        MqttClientAdapterFactoryRegistry factoryRegistry = new MqttClientAdapterFactoryRegistry(List.of(new StubFactory(adapter)));
        DefaultMqttClientAdapterRegistry adapterRegistry = new DefaultMqttClientAdapterRegistry();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new MqttBrokerAutoConfiguration().registerAdapters(
                        properties,
                        factoryRegistry,
                        adapterRegistry,
                        (brokerId, topic, payload, headers) -> { },
                        List.of(new NoOpConnectionListener())));

        assertEquals("connect failed", exception.getMessage());
        assertFalse(adapterRegistry.find("primary").isPresent());
    }

    private static MqttPlusProperties createProperties() {
        MqttPlusProperties properties = new MqttPlusProperties();
        MqttPlusProperties.BrokerProperties broker = new MqttPlusProperties.BrokerProperties();
        broker.setHost("127.0.0.1");
        broker.setPort(1883);
        broker.setClientId("starter-test");
        properties.getBrokers().put("primary", broker);
        return properties;
    }

    private static final class StubFactory implements MqttClientAdapterFactory {
        private final StubAdapter adapter;

        private StubFactory(StubAdapter adapter) {
            this.adapter = adapter;
        }

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
            return adapter;
        }
    }

    private static final class StubAdapter implements MqttClientAdapter {
        private final MqttBrokerDefinition brokerDefinition;
        private final boolean failOnConnect;
        private final List<MqttConnectionListener> connectionListeners = new ArrayList<>();
        private boolean connected;

        private StubAdapter(MqttBrokerDefinition brokerDefinition, boolean failOnConnect) {
            this.brokerDefinition = brokerDefinition;
            this.failOnConnect = failOnConnect;
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
            if (failOnConnect) {
                throw new IllegalStateException("connect failed");
            }
            connected = true;
        }

        @Override
        public void disconnect() {
            connected = false;
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
        public boolean supportsManualAck() {
            return false;
        }

        @Override
        public void addConnectionListener(MqttConnectionListener listener) {
            connectionListeners.add(listener);
        }
    }

    private static final class NoOpConnectionListener implements MqttConnectionListener {
        @Override
        public void onConnected(String brokerId) {
        }

        @Override
        public void onConnectionLost(String brokerId, Throwable cause) {
        }

        @Override
        public void onDisconnected(String brokerId) {
        }
    }
}

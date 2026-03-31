package io.github.mqttplus.paho;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PahoMqttClientAdapterFactoryTest {

    @Test
    void shouldCreatePahoAdapterFromBrokerDefinition() {
        PahoMqttClientAdapterFactory factory = new PahoMqttClientAdapterFactory();
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("127.0.0.1")
                .port(1883)
                .clientId("paho-test")
                .inboundThreadPool(ThreadPoolConfig.builder().build())
                .build();

        MqttClientAdapter adapter = factory.create(definition, (brokerId, topic, payload, headers) -> {
        });

        assertEquals(PahoMqttClientAdapterFactory.SUPPORTED_VERSION, factory.supportedVersion());
        assertInstanceOf(PahoMqttClientAdapter.class, adapter);
        assertEquals("primary", adapter.getBrokerId());
        assertFalse(adapter.supportsManualAck());
    }

    @Test
    void shouldBuildServerUriAndConnectionOptionsFromBrokerDefinition() throws Exception {
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("secure")
                .host("broker.example.com")
                .port(8883)
                .clientId("secure-client")
                .username("mqtt-user")
                .password("secret")
                .sslEnabled(true)
                .keepAliveInterval(45)
                .connectionTimeout(12)
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();

        PahoMqttClientAdapter adapter = new PahoMqttClientAdapter(definition, (brokerId, topic, payload, headers) -> {
        });

        assertEquals("ssl://broker.example.com:8883", adapter.getServerUri());
        assertEquals("mqtt-user", adapter.getConnectOptions().getUserName());
        assertNotNull(adapter.getConnectOptions().getPassword());
        assertEquals(45, adapter.getConnectOptions().getKeepAliveInterval());
        assertEquals(12, adapter.getConnectOptions().getConnectionTimeout());
        assertFalse(adapter.getConnectOptions().isAutomaticReconnect());
    }

    @Test
    void shouldNotifyListenersAndDispatchInboundMessagesOffCallbackThread() throws Exception {
        AtomicReference<String> connectedBroker = new AtomicReference<>();
        AtomicReference<String> lostBroker = new AtomicReference<>();
        AtomicReference<Throwable> lostCause = new AtomicReference<>();
        AtomicReference<String> inboundBroker = new AtomicReference<>();
        AtomicReference<String> inboundTopic = new AtomicReference<>();
        AtomicReference<byte[]> inboundPayload = new AtomicReference<>();
        AtomicReference<Object> inboundQos = new AtomicReference<>();
        CountDownLatch messageLatch = new CountDownLatch(1);
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("127.0.0.1")
                .clientId("listener-test")
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();
        PahoMqttClientAdapter adapter = new PahoMqttClientAdapter(definition, (brokerId, topic, payload, headers) -> {
            inboundBroker.set(brokerId);
            inboundTopic.set(topic);
            inboundPayload.set(payload);
            inboundQos.set(headers.get("qos"));
            messageLatch.countDown();
        });
        adapter.addConnectionListener(new MqttConnectionListener() {
            @Override
            public void onConnected(String brokerId) {
                connectedBroker.set(brokerId);
            }

            @Override
            public void onConnectionLost(String brokerId, Throwable cause) {
                lostBroker.set(brokerId);
                lostCause.set(cause);
            }

            @Override
            public void onDisconnected(String brokerId) {
            }
        });

        adapter.handleConnectComplete(false, adapter.getServerUri());
        RuntimeException cause = new RuntimeException("network");
        adapter.handleConnectionLost(cause);
        MqttMessage message = new MqttMessage("hello".getBytes(StandardCharsets.UTF_8));
        message.setQos(1);
        adapter.handleMessage("devices/1/status", message);

        assertEquals("primary", connectedBroker.get());
        assertEquals("primary", lostBroker.get());
        assertEquals(cause, lostCause.get());
        assertTrue(messageLatch.await(2, TimeUnit.SECONDS));
        assertEquals("primary", inboundBroker.get());
        assertEquals("devices/1/status", inboundTopic.get());
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), inboundPayload.get());
        assertEquals(1, inboundQos.get());
    }
}

package io.github.mqttplus.hivemq;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HiveMqMqttClientAdapterTest {

    @Test
    void shouldBuildConnectMessageFromBrokerDefinition() {
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("secure")
                .host("broker.example.com")
                .port(8883)
                .clientId("mqtt5-client")
                .username("mqtt-user")
                .password("secret")
                .sslEnabled(true)
                .cleanSession(false)
                .keepAliveInterval(45)
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();

        Mqtt5Connect connect = HiveMqMqttClientAdapter.buildConnectMessage(definition);

        assertEquals(45, connect.getKeepAlive());
        assertFalse(connect.isCleanStart());
        assertEquals(Mqtt5Connect.NO_SESSION_EXPIRY, connect.getSessionExpiryInterval());
        assertEquals("mqtt-user", connect.getSimpleAuth().orElseThrow().getUsername().orElseThrow().toString());
        byte[] passwordBytes = new byte[connect.getSimpleAuth().orElseThrow().getPassword().orElseThrow().remaining()];
        connect.getSimpleAuth().orElseThrow().getPassword().orElseThrow().duplicate().get(passwordBytes);
        assertArrayEquals("secret".getBytes(StandardCharsets.UTF_8), passwordBytes);
    }

    @Test
    void shouldDelegateConnectSubscribePublishAndDisconnect() {
        Mqtt5AsyncClient client = mock(Mqtt5AsyncClient.class);
        when(client.connect(any())).thenReturn(CompletableFuture.completedFuture(mock(Mqtt5ConnAck.class)));
        when(client.subscribe(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(mock(Mqtt5SubAck.class)));
        when(client.publish(any())).thenReturn(CompletableFuture.completedFuture(mock(Mqtt5PublishResult.class)));
        when(client.disconnect()).thenReturn(CompletableFuture.completedFuture(null));

        AtomicReference<String> connectedBroker = new AtomicReference<>();
        AtomicReference<String> disconnectedBroker = new AtomicReference<>();
        HiveMqMqttClientAdapter adapter = new HiveMqMqttClientAdapter(
                createDefinition(),
                (brokerId, topic, payload, headers) -> {
                },
                client,
                Mqtt5Connect.builder().build());
        adapter.addConnectionListener(new MqttConnectionListener() {
            @Override
            public void onConnected(String brokerId) {
                connectedBroker.set(brokerId);
            }

            @Override
            public void onConnectionLost(String brokerId, Throwable cause) {
            }

            @Override
            public void onDisconnected(String brokerId) {
                disconnectedBroker.set(brokerId);
            }
        });

        adapter.connect();
        adapter.subscribe("devices/1/status", 1);
        adapter.publish("devices/1/status", "payload".getBytes(StandardCharsets.UTF_8), 1, true);
        adapter.disconnect();

        verify(client).connect(any());
        verify(client).subscribe(any(), any(), any());
        verify(client).publish(any());
        verify(client).disconnect();
        assertEquals("primary", connectedBroker.get());
        assertEquals("primary", disconnectedBroker.get());
    }

    @Test
    void shouldBuildPublishAndForwardInboundMessage() {
        AtomicReference<String> brokerRef = new AtomicReference<>();
        AtomicReference<String> topicRef = new AtomicReference<>();
        AtomicReference<byte[]> payloadRef = new AtomicReference<>();
        AtomicReference<Object> qosRef = new AtomicReference<>();
        AtomicReference<Object> retainedRef = new AtomicReference<>();
        HiveMqMqttClientAdapter adapter = new HiveMqMqttClientAdapter(
                createDefinition(),
                (brokerId, topic, payload, headers) -> {
                    brokerRef.set(brokerId);
                    topicRef.set(topic);
                    payloadRef.set(payload);
                    qosRef.set(headers.get("qos"));
                    retainedRef.set(headers.get("retained"));
                },
                mock(Mqtt5AsyncClient.class),
                Mqtt5Connect.builder().build());

        Mqtt5Publish publish = adapter.buildPublish("devices/1/status", "hello".getBytes(StandardCharsets.UTF_8), 1, true);
        adapter.handlePublish(publish);

        assertEquals("devices/1/status", publish.getTopic().toString());
        assertEquals(MqttQos.AT_LEAST_ONCE, publish.getQos());
        assertTrue(publish.isRetain());
        assertEquals("primary", brokerRef.get());
        assertEquals("devices/1/status", topicRef.get());
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), payloadRef.get());
        assertEquals(1, qosRef.get());
        assertEquals(true, retainedRef.get());
    }

    @Test
    void shouldNotifyConnectionLostForNonUserDisconnects() {
        MqttConnectionListener listener = mock(MqttConnectionListener.class);
        RuntimeException cause = new RuntimeException("network down");
        List<MqttConnectionListener> listeners = new ArrayList<>();
        listeners.add(listener);

        HiveMqMqttClientAdapter.handleDisconnected(MqttDisconnectSource.SERVER, cause, createDefinition(), listeners);

        verify(listener).onConnectionLost("primary", cause);
    }

    @Test
    void shouldIgnoreUserInitiatedDisconnectsForConnectionLostCallback() {
        MqttConnectionListener listener = mock(MqttConnectionListener.class);
        List<MqttConnectionListener> listeners = new ArrayList<>();
        listeners.add(listener);

        HiveMqMqttClientAdapter.handleDisconnected(MqttDisconnectSource.USER, null, createDefinition(), listeners);

        verify(listener, never()).onConnectionLost(any(), any());
    }

    private static MqttBrokerDefinition createDefinition() {
        return MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("127.0.0.1")
                .port(1883)
                .clientId("hivemq-test")
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();
    }
}

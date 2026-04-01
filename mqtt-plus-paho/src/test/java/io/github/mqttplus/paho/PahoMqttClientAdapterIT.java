package io.github.mqttplus.paho;

import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class PahoMqttClientAdapterIT {

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mosquitto/mosquitto.conf"),
                    "/mosquitto/config/mosquitto.conf")
            .withExposedPorts(1883)
            .withStartupTimeout(Duration.ofSeconds(30));

    private PahoMqttClientAdapter adapter;

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.disconnect();
        }
    }

    @Test
    void shouldReceiveMessagesFromRealBroker() throws Exception {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<String> inboundBroker = new AtomicReference<>();
        AtomicReference<String> inboundTopic = new AtomicReference<>();
        AtomicReference<byte[]> inboundPayload = new AtomicReference<>();
        AtomicReference<Object> inboundQos = new AtomicReference<>();

        adapter = new PahoMqttClientAdapter(createDefinition("receive-test"), (brokerId, topic, payload, headers) -> {
            inboundBroker.set(brokerId);
            inboundTopic.set(topic);
            inboundPayload.set(payload);
            inboundQos.set(headers.get("qos"));
            messageLatch.countDown();
        });
        adapter.addConnectionListener(new MqttConnectionListener() {
            @Override
            public void onConnected(String brokerId) {
                connectedLatch.countDown();
            }

            @Override
            public void onConnectionLost(String brokerId, Throwable cause) {
            }

            @Override
            public void onDisconnected(String brokerId) {
            }
        });

        adapter.connect();
        assertTrue(connectedLatch.await(5, TimeUnit.SECONDS));

        String topic = "devices/" + UUID.randomUUID() + "/status";
        adapter.subscribe("devices/+/status", 1);
        publishWithRawClient(topic, "hello".getBytes(StandardCharsets.UTF_8), 1, false);

        assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
        assertEquals("primary", inboundBroker.get());
        assertEquals(topic, inboundTopic.get());
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), inboundPayload.get());
        assertEquals(1, inboundQos.get());
    }

    @Test
    void shouldPublishWithConfiguredQosAndRetainedFlags() throws Exception {
        CountDownLatch messageLatch = new CountDownLatch(1);
        AtomicReference<MqttMessage> receivedMessage = new AtomicReference<>();
        String topic = "devices/" + UUID.randomUUID() + "/command";

        try (MqttClient subscriber = newRawClient("subscriber-" + UUID.randomUUID())) {
            subscriber.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                }

                @Override
                public void messageArrived(String arrivedTopic, MqttMessage message) {
                    if (topic.equals(arrivedTopic)) {
                        receivedMessage.set(message);
                        messageLatch.countDown();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            subscriber.connect(connectOptions());
            subscriber.subscribe(topic, 1);

            adapter = new PahoMqttClientAdapter(createDefinition("publish-test"), (brokerId, arrivedTopic, payload, headers) -> {
            });
            adapter.connect();
            adapter.publish(topic, "payload", 1, true);

            assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
            assertEquals(1, receivedMessage.get().getQos());
            assertTrue(receivedMessage.get().isRetained());
            assertArrayEquals("payload".getBytes(StandardCharsets.UTF_8), receivedMessage.get().getPayload());
        }
    }

    private MqttBrokerDefinition createDefinition(String clientIdSuffix) {
        return MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host(MOSQUITTO.getHost())
                .port(MOSQUITTO.getMappedPort(1883))
                .clientId("mqtt-plus-" + clientIdSuffix + "-" + UUID.randomUUID())
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();
    }

    private void publishWithRawClient(String topic, byte[] payload, int qos, boolean retained) throws MqttException {
        try (MqttClient publisher = newRawClient("publisher-" + UUID.randomUUID())) {
            publisher.connect(connectOptions());
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            message.setRetained(retained);
            publisher.publish(topic, message);
        }
    }

    private MqttClient newRawClient(String clientId) throws MqttException {
        return new MqttClient(serverUri(), clientId, new MemoryPersistence());
    }

    private MqttConnectOptions connectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        return options;
    }

    private String serverUri() {
        return "tcp://" + MOSQUITTO.getHost() + ":" + MOSQUITTO.getMappedPort(1883);
    }
}

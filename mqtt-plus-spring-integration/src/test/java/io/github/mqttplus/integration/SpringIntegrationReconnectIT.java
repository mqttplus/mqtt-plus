package io.github.mqttplus.integration;

import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class SpringIntegrationReconnectIT {

    private static final Network NETWORK = Network.newNetwork();

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mosquitto/mosquitto.conf"),
                    "/mosquitto/config/mosquitto.conf")
            .withExposedPorts(1883)
            .withNetwork(NETWORK)
            .withNetworkAliases("mosquitto")
            .waitingFor(Wait.forLogMessage("(?s).*mosquitto version .* running.*", 1))
            .withStartupTimeout(Duration.ofSeconds(30));

    @Container
    static final ToxiproxyContainer TOXIPROXY = new ToxiproxyContainer(DockerImageName.parse("ghcr.io/shopify/toxiproxy:2.12.0"))
            .withNetwork(NETWORK);

    private static ToxiproxyContainer.ContainerProxy mqttProxy;

    private SpringIntegrationMqttClientAdapter adapter;

    @BeforeAll
    static void setUpProxy() {
        mqttProxy = TOXIPROXY.getProxy(MOSQUITTO, 1883);
    }

    @AfterEach
    void tearDown() {
        if (adapter != null) {
            adapter.disconnect();
        }
        if (mqttProxy != null) {
            mqttProxy.setConnectionCut(false);
        }
    }

    @Test
    void shouldRecoverSubscriptionsAfterNetworkInterruption() throws Exception {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch recoveredMessageLatch = new CountDownLatch(1);
        AtomicInteger inboundCount = new AtomicInteger();
        AtomicReference<String> recoveredTopic = new AtomicReference<>();
        String topic = "devices/" + UUID.randomUUID() + "/reconnect";

        adapter = new SpringIntegrationMqttClientAdapter(createDefinition("reconnect-test"), (brokerId, arrivedTopic, payload, headers) -> {
            inboundCount.incrementAndGet();
            recoveredTopic.set(arrivedTopic);
            recoveredMessageLatch.countDown();
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
        adapter.subscribe(topic, 1);
        waitForBrokerProcessing();

        mqttProxy.setConnectionCut(true);
        Thread.sleep(3000);
        mqttProxy.setConnectionCut(false);

        boolean recovered = false;
        for (int attempt = 0; attempt < 10 && !recovered; attempt++) {
            publishWithRawClient(topic, ("recovered-" + attempt).getBytes(StandardCharsets.UTF_8), 1, false);
            recovered = recoveredMessageLatch.await(1, TimeUnit.SECONDS);
        }

        assertTrue(recovered, "expected subscription to receive a message after network recovery");
        assertEquals(topic, recoveredTopic.get());
        assertTrue(inboundCount.get() >= 1);
    }

    private MqttBrokerDefinition createDefinition(String clientIdSuffix) {
        return MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host(TOXIPROXY.getHost())
                .port(mqttProxy.getProxyPort())
                .clientId("mqtt-plus-integration-" + clientIdSuffix + "-" + UUID.randomUUID())
                .connectionTimeout(3)
                .keepAliveInterval(2)
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
            if (publisher.isConnected()) {
                publisher.disconnect();
            }
        }
    }

    private MqttClient newRawClient(String clientId) throws MqttException {
        return new MqttClient("tcp://" + MOSQUITTO.getHost() + ":" + MOSQUITTO.getMappedPort(1883), clientId, new MemoryPersistence());
    }

    private MqttConnectOptions connectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        options.setCleanSession(true);
        return options;
    }

    private void waitForBrokerProcessing() {
        try {
            Thread.sleep(250);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for broker processing", ex);
        }
    }
}
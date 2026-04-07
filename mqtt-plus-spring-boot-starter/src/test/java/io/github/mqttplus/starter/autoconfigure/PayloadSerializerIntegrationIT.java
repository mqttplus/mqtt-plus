package io.github.mqttplus.starter.autoconfigure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqttplus.core.MqttTemplate;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class PayloadSerializerIntegrationIT {

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0"))
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("mosquitto/mosquitto.conf"),
                    "/mosquitto/config/mosquitto.conf")
            .withExposedPorts(1883)
            .waitingFor(Wait.forLogMessage("(?s).*mosquitto version .* running.*", 1))
            .withStartupTimeout(Duration.ofSeconds(30));

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class));

    @Test
    void shouldSerializePojoPayloadToJsonBytesWhenPublishingThroughTemplate() {
        String topic = "devices/" + UUID.randomUUID() + "/command";

        contextRunner
                .withPropertyValues(
                        "mqtt-plus.brokers.primary.host=" + MOSQUITTO.getHost(),
                        "mqtt-plus.brokers.primary.port=" + MOSQUITTO.getMappedPort(1883),
                        "mqtt-plus.brokers.primary.client-id=serializer-it-" + UUID.randomUUID(),
                        "mqtt-plus.brokers.primary.adapter=paho")
                .run(context -> {
                    MqttTemplate template = context.getBean(MqttTemplate.class);
                    CountDownLatch messageLatch = new CountDownLatch(1);
                    AtomicReference<byte[]> receivedPayload = new AtomicReference<>();

                    try (MqttClient subscriber = newRawClient("subscriber-" + UUID.randomUUID())) {
                        subscriber.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                            }

                            @Override
                            public void messageArrived(String arrivedTopic, MqttMessage message) {
                                if (topic.equals(arrivedTopic)) {
                                    receivedPayload.set(message.getPayload());
                                    messageLatch.countDown();
                                }
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                            }
                        });
                        subscriber.connect(connectOptions());
                        subscriber.subscribe(topic, 1);
                        waitForBrokerProcessing();

                        publishWithRetry(template, topic, new DeviceCommand("land", 1));

                        assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
                        String json = new String(receivedPayload.get(), StandardCharsets.UTF_8);
                        assertFalse(json.contains("DeviceCommand"));

                        Map<String, Object> payloadMap = new ObjectMapper().readValue(receivedPayload.get(), new TypeReference<Map<String, Object>>() {
                        });
                        assertEquals("land", payloadMap.get("action"));
                        assertEquals(1, ((Number) payloadMap.get("qos")).intValue());

                        if (subscriber.isConnected()) {
                            subscriber.disconnect();
                        }
                    }
                });
    }

    private void publishWithRetry(MqttTemplate template, String topic, Object payload) throws InterruptedException {
        IllegalStateException lastError = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                template.publish("primary", topic, payload, 1, false);
                return;
            }
            catch (IllegalStateException ex) {
                lastError = ex;
                Thread.sleep(300);
            }
        }
        throw lastError;
    }

    private MqttClient newRawClient(String clientId) throws MqttException {
        return new MqttClient(serverUri(), clientId, new MemoryPersistence());
    }

    private MqttConnectOptions connectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        options.setCleanSession(true);
        return options;
    }

    private String serverUri() {
        return "tcp://" + MOSQUITTO.getHost() + ":" + MOSQUITTO.getMappedPort(1883);
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

    private static final class DeviceCommand {
        private final String action;
        private final int qos;

        private DeviceCommand(String action, int qos) {
            this.action = action;
            this.qos = qos;
        }

        public String getAction() {
            return action;
        }

        public int getQos() {
            return qos;
        }
    }
}
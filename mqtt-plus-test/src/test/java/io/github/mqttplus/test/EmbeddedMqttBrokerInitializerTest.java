package io.github.mqttplus.test;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddedMqttBrokerInitializerTest {

    @Test
    void shouldRegisterEmbeddedBrokerAndExposeProperties() throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        EmbeddedMqttBrokerInitializer initializer = new EmbeddedMqttBrokerInitializer();
        initializer.initialize(context);
        EmbeddedMqttBroker broker = (EmbeddedMqttBroker) context.getBeanFactory().getSingleton("embeddedMqttBroker");

        try {
            assertNotNull(broker);
            assertTrue(broker.isStarted());
            assertEquals(broker.getHost(), context.getEnvironment().getProperty("mqtt-plus.brokers.primary.host"));
            assertEquals(Integer.toString(broker.getPort()), context.getEnvironment().getProperty("mqtt-plus.brokers.primary.port"));
            assertNotNull(context.getEnvironment().getProperty("mqtt-plus.brokers.primary.client-id"));
        } finally {
            broker.stop();
            context.close();
        }
    }

    @Test
    void shouldAcceptRawMqttClientsAgainstEmbeddedBroker() throws Exception {
        try (EmbeddedMqttBroker broker = EmbeddedMqttBroker.startDefault()) {
            String topic = "mqtt-plus/test/" + UUID.randomUUID();
            CountDownLatch messageLatch = new CountDownLatch(1);
            AtomicReference<String> receivedPayload = new AtomicReference<>();

            try (MqttClient subscriber = newClient(broker, "subscriber-" + UUID.randomUUID());
                 MqttClient publisher = newClient(broker, "publisher-" + UUID.randomUUID())) {
                subscriber.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                    }

                    @Override
                    public void messageArrived(String arrivedTopic, MqttMessage message) {
                        if (topic.equals(arrivedTopic)) {
                            receivedPayload.set(new String(message.getPayload(), StandardCharsets.UTF_8));
                            messageLatch.countDown();
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                subscriber.connect(connectOptions());
                subscriber.subscribe(topic, 0);

                publisher.connect(connectOptions());
                publisher.publish(topic, new MqttMessage("hello".getBytes(StandardCharsets.UTF_8)));

                assertTrue(messageLatch.await(5, TimeUnit.SECONDS));
                assertEquals("hello", receivedPayload.get());

                if (publisher.isConnected()) {
                    publisher.disconnect();
                }
                if (subscriber.isConnected()) {
                    subscriber.disconnect();
                }
            }
        }
    }

    private MqttClient newClient(EmbeddedMqttBroker broker, String clientId) throws Exception {
        return new MqttClient("tcp://" + broker.getHost() + ":" + broker.getPort(), clientId, new MemoryPersistence());
    }

    private MqttConnectOptions connectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        options.setCleanSession(true);
        return options;
    }
}
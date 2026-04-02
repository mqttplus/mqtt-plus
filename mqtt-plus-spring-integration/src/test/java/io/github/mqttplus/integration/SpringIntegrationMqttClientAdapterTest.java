package io.github.mqttplus.integration;

import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.jupiter.api.Test;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringIntegrationMqttClientAdapterTest {

    @Test
    void shouldBuildClientFactoryWithCleanSessionAndConnectionOptions() {
        MqttBrokerDefinition definition = MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("broker.example.com")
                .port(1883)
                .clientId("integration-client")
                .username("mqtt-user")
                .password("secret")
                .cleanSession(false)
                .keepAliveInterval(45)
                .connectionTimeout(12)
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();

        SpringIntegrationMqttClientAdapter adapter =
                new SpringIntegrationMqttClientAdapter(definition, (brokerId, topic, payload, headers) -> {
                });

        MqttConnectOptions options = adapter.getClientFactory().getConnectionOptions();

        assertEquals("tcp://broker.example.com:1883", adapter.getServerUri());
        assertEquals("mqtt-user", options.getUserName());
        assertEquals(45, options.getKeepAliveInterval());
        assertEquals(12, options.getConnectionTimeout());
        assertFalse(options.isCleanSession());
        assertArrayEquals("secret".toCharArray(), options.getPassword());
    }

    @Test
    void shouldStartStopAndDelegateSubscribePublishOperations() {
        MqttPahoMessageDrivenChannelAdapter inboundAdapter = mock(MqttPahoMessageDrivenChannelAdapter.class);
        MqttPahoMessageHandler outboundHandler = mock(MqttPahoMessageHandler.class);
        DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
        AtomicReference<String> connectedBroker = new AtomicReference<>();
        AtomicReference<String> disconnectedBroker = new AtomicReference<>();

        SpringIntegrationMqttClientAdapter adapter = new SpringIntegrationMqttClientAdapter(
                createDefinition(),
                (brokerId, topic, payload, headers) -> {
                },
                clientFactory,
                inboundAdapter,
                outboundHandler,
                "tcp://127.0.0.1:1883");
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
        adapter.publish("devices/1/status", "payload", 1, true);
        adapter.unsubscribe("devices/1/status");
        adapter.disconnect();

        verify(outboundHandler).start();
        verify(inboundAdapter).start();
        verify(inboundAdapter).addTopic("devices/1/status", 1);
        verify(inboundAdapter).removeTopic("devices/1/status");
        verify(inboundAdapter).stop();
        verify(outboundHandler).stop();
        assertEquals("primary", connectedBroker.get());
        assertEquals("primary", disconnectedBroker.get());

        verify(outboundHandler, times(1)).handleMessage(any(Message.class));
    }

    @Test
    void shouldForwardInboundMessageToSink() {
        AtomicReference<String> brokerRef = new AtomicReference<>();
        AtomicReference<String> topicRef = new AtomicReference<>();
        AtomicReference<byte[]> payloadRef = new AtomicReference<>();
        AtomicReference<Object> qosRef = new AtomicReference<>();
        AtomicReference<Object> retainedRef = new AtomicReference<>();
        MqttPahoMessageDrivenChannelAdapter inboundAdapter = mock(MqttPahoMessageDrivenChannelAdapter.class);
        MqttPahoMessageHandler outboundHandler = mock(MqttPahoMessageHandler.class);

        SpringIntegrationMqttClientAdapter adapter = new SpringIntegrationMqttClientAdapter(
                createDefinition(),
                (brokerId, topic, payload, headers) -> {
                    brokerRef.set(brokerId);
                    topicRef.set(topic);
                    payloadRef.set(payload);
                    qosRef.set(headers.get("qos"));
                    retainedRef.set(headers.get("retained"));
                },
                new DefaultMqttPahoClientFactory(),
                inboundAdapter,
                outboundHandler,
                "tcp://127.0.0.1:1883");

        adapter.handleInboundMessage(MessageBuilder.withPayload("hello")
                .setHeader(org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_TOPIC, "devices/1/status")
                .setHeader(org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_QOS, 1)
                .setHeader(org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_RETAINED, true)
                .build());

        assertEquals("primary", brokerRef.get());
        assertEquals("devices/1/status", topicRef.get());
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), payloadRef.get());
        assertEquals(1, qosRef.get());
        assertEquals(true, retainedRef.get());
    }

    private static MqttBrokerDefinition createDefinition() {
        return MqttBrokerDefinition.builder()
                .brokerId("primary")
                .host("127.0.0.1")
                .port(1883)
                .clientId("integration-test")
                .inboundThreadPool(ThreadPoolConfig.builder().coreSize(1).build())
                .build();
    }
}
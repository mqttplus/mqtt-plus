package io.github.mqttplus.hivemq;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.MqttHeaders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class HiveMqMqttClientAdapter implements MqttClientAdapter {

    private final MqttBrokerDefinition brokerDefinition;
    private final MqttInboundMessageSink inboundMessageSink;
    private final List<MqttConnectionListener> connectionListeners;
    private final ExecutorService inboundExecutor;
    private final Mqtt5AsyncClient mqttClient;
    private final Mqtt5Connect connectMessage;

    public HiveMqMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                                   MqttInboundMessageSink inboundMessageSink) {
        this(brokerDefinition, inboundMessageSink, new ArrayList<>());
    }

    private HiveMqMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                                    MqttInboundMessageSink inboundMessageSink,
                                    List<MqttConnectionListener> connectionListeners) {
        this(
                brokerDefinition,
                inboundMessageSink,
                connectionListeners,
                buildClient(brokerDefinition, connectionListeners),
                buildConnectMessage(brokerDefinition));
    }

    HiveMqMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                            MqttInboundMessageSink inboundMessageSink,
                            Mqtt5AsyncClient mqttClient,
                            Mqtt5Connect connectMessage) {
        this(brokerDefinition, inboundMessageSink, new ArrayList<>(), mqttClient, connectMessage);
    }

    HiveMqMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                            MqttInboundMessageSink inboundMessageSink,
                            List<MqttConnectionListener> connectionListeners,
                            Mqtt5AsyncClient mqttClient,
                            Mqtt5Connect connectMessage) {
        this.brokerDefinition = Objects.requireNonNull(brokerDefinition, "brokerDefinition must not be null");
        this.inboundMessageSink = Objects.requireNonNull(inboundMessageSink, "inboundMessageSink must not be null");
        this.connectionListeners = Objects.requireNonNull(connectionListeners, "connectionListeners must not be null");
        this.mqttClient = Objects.requireNonNull(mqttClient, "mqttClient must not be null");
        this.connectMessage = Objects.requireNonNull(connectMessage, "connectMessage must not be null");
        this.inboundExecutor = Executors.newFixedThreadPool(brokerDefinition.getInboundThreadPool().getCoreSize());
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
        try {
            mqttClient.connect(connectMessage).join();
            notifyConnected();
        } catch (CompletionException ex) {
            throw failure("Failed to connect broker: " + brokerDefinition.getBrokerId(), ex);
        }
    }

    @Override
    public void disconnect() {
        try {
            mqttClient.disconnect().join();
            notifyDisconnected();
        } catch (CompletionException ex) {
            throw failure("Failed to disconnect broker: " + brokerDefinition.getBrokerId(), ex);
        } finally {
            inboundExecutor.shutdown();
        }
    }

    @Override
    public void subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(buildSubscribe(topic, qos), this::handlePublish, inboundExecutor).join();
        } catch (CompletionException ex) {
            throw failure("Failed to subscribe topic: " + topic, ex);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            mqttClient.unsubscribeWith().addTopicFilter(topic).send().join();
        } catch (CompletionException ex) {
            throw failure("Failed to unsubscribe topic: " + topic, ex);
        }
    }

    @Override
    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 0, false);
    }

    @Override
    public void publish(String topic, byte[] payload, int qos, boolean retained) {
        try {
            mqttClient.publish(buildPublish(topic, payload, qos, retained)).join();
        } catch (CompletionException ex) {
            throw failure("Failed to publish topic: " + topic, ex);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload) {
        return publishAsync(topic, payload, 0, false);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained) {
        return mqttClient.publish(buildPublish(topic, payload, qos, retained)).thenApply(this::ignorePublishResult);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload, Executor executor) {
        return publishAsync(topic, payload, 0, false, executor);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained, Executor executor) {
        return CompletableFuture.runAsync(() -> publish(topic, payload, qos, retained), executor);
    }

    @Override
    public boolean supportsManualAck() {
        return false;
    }

    @Override
    public void addConnectionListener(MqttConnectionListener listener) {
        connectionListeners.add(listener);
    }

    Mqtt5Connect getConnectMessage() {
        return connectMessage;
    }

    Mqtt5Publish buildPublish(String topic, byte[] payload, int qos, boolean retained) {
        if (payload == null) {
            throw new IllegalArgumentException("payload bytes must not be null");
        }
        return Mqtt5Publish.builder()
                .topic(topic)
                .qos(MqttQos.fromCode(qos))
                .payload(payload)
                .retain(retained)
                .build();
    }

    Mqtt5Subscribe buildSubscribe(String topic, int qos) {
        return Mqtt5Subscribe.builder()
                .addSubscription(Mqtt5Subscription.builder()
                        .topicFilter(topic)
                        .qos(MqttQos.fromCode(qos))
                        .build())
                .build();
    }

    void handlePublish(Mqtt5Publish publish) {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("qos", publish.getQos().getCode());
        headers.put("retained", publish.isRetain());
        inboundMessageSink.onMessage(
                getBrokerId(),
                publish.getTopic().toString(),
                publish.getPayloadAsBytes(),
                new MqttHeaders(headers));
    }

    private Void ignorePublishResult(Mqtt5PublishResult result) {
        return null;
    }

    private static Mqtt5AsyncClient buildClient(MqttBrokerDefinition brokerDefinition,
                                                List<MqttConnectionListener> connectionListeners) {
        Mqtt5ClientBuilder builder = MqttClient.builder()
                .useMqttVersion5()
                .identifier(brokerDefinition.getClientId())
                .serverHost(brokerDefinition.getHost())
                .serverPort(brokerDefinition.getPort())
                .transportConfig()
                    .socketConnectTimeout(brokerDefinition.getConnectionTimeout(), TimeUnit.SECONDS)
                    .mqttConnectTimeout(brokerDefinition.getConnectionTimeout(), TimeUnit.SECONDS)
                    .applyTransportConfig();
        if (brokerDefinition.isSslEnabled()) {
            builder.sslWithDefaultConfig();
        }
        builder.addDisconnectedListener(context ->
                handleDisconnected(context.getSource(), context.getCause(), brokerDefinition, connectionListeners));
        return builder.buildAsync();
    }

    static Mqtt5Connect buildConnectMessage(MqttBrokerDefinition brokerDefinition) {
        Mqtt5ConnectBuilder builder = Mqtt5Connect.builder()
                .keepAlive(brokerDefinition.getKeepAliveInterval())
                .cleanStart(brokerDefinition.isCleanSession());
        if (brokerDefinition.isCleanSession()) {
            builder.sessionExpiryInterval(0);
        } else {
            builder.noSessionExpiry();
        }
        if (brokerDefinition.getUsername() != null && !brokerDefinition.getUsername().isBlank()) {
            var authBuilder = builder.simpleAuth()
                    .username(brokerDefinition.getUsername());
            if (brokerDefinition.getPassword() != null) {
                authBuilder.password(brokerDefinition.getPassword().getBytes(StandardCharsets.UTF_8));
            }
            builder = authBuilder.applySimpleAuth();
        }
        return builder.build();
    }

    private static IllegalStateException failure(String message, CompletionException ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        return new IllegalStateException(message, cause);
    }

    static void handleDisconnected(MqttDisconnectSource source,
                                   Throwable cause,
                                   MqttBrokerDefinition brokerDefinition,
                                   List<MqttConnectionListener> connectionListeners) {
        if (source == MqttDisconnectSource.USER) {
            return;
        }
        for (MqttConnectionListener listener : connectionListeners) {
            listener.onConnectionLost(brokerDefinition.getBrokerId(), cause);
        }
    }

    private void notifyConnected() {
        for (MqttConnectionListener listener : connectionListeners) {
            listener.onConnected(getBrokerId());
        }
    }

    private void notifyDisconnected() {
        for (MqttConnectionListener listener : connectionListeners) {
            listener.onDisconnected(getBrokerId());
        }
    }
}

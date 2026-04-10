package io.github.mqttplus.paho;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.MqttHeaders;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PahoMqttClientAdapter implements MqttClientAdapter {

    private final MqttBrokerDefinition brokerDefinition;
    private final MqttInboundMessageSink inboundMessageSink;
    private final List<MqttConnectionListener> connectionListeners = new ArrayList<>();
    private final ExecutorService inboundExecutor;
    private final MqttClient mqttClient;
    private final MqttConnectOptions connectOptions;
    private final String serverUri;

    public PahoMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                                 MqttInboundMessageSink inboundMessageSink) throws MqttException {
        this.brokerDefinition = Objects.requireNonNull(brokerDefinition, "brokerDefinition must not be null");
        this.inboundMessageSink = Objects.requireNonNull(inboundMessageSink, "inboundMessageSink must not be null");
        this.inboundExecutor = Executors.newFixedThreadPool(brokerDefinition.getInboundThreadPool().getCoreSize());
        this.serverUri = buildServerUri(brokerDefinition);
        this.mqttClient = new MqttClient(serverUri, brokerDefinition.getClientId(), new MemoryPersistence());
        this.connectOptions = buildConnectOptions(brokerDefinition);
        this.mqttClient.setCallback(new CallbackHandler());
    }

    @Override
    public void addConnectionListener(MqttConnectionListener listener) {
        this.connectionListeners.add(listener);
    }

    String getServerUri() {
        return serverUri;
    }

    MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    MqttMessage toMessageForTesting(byte[] payload, int qos, boolean retained) {
        return toMessage(payload, qos, retained);
    }

    void handleConnectComplete(boolean reconnect, String connectedServerUri) {
        notifyConnected();
    }

    void handleConnectionLost(Throwable cause) {
        notifyConnectionLost(cause);
    }

    void handleMessage(String topic, MqttMessage message) {
        byte[] payload = message.getPayload();
        inboundExecutor.submit(() -> inboundMessageSink.onMessage(getBrokerId(), topic, payload, new MqttHeaders(Map.of("qos", message.getQos()))));
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
            mqttClient.connect(connectOptions);
        }
        catch (MqttException ex) {
            throw new IllegalStateException("Failed to connect broker: " + brokerDefinition.getBrokerId(), ex);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            inboundExecutor.shutdown();
            notifyDisconnected();
        }
        catch (MqttException ex) {
            throw new IllegalStateException("Failed to disconnect broker: " + brokerDefinition.getBrokerId(), ex);
        }
    }

    @Override
    public void subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
        }
        catch (MqttException ex) {
            throw new IllegalStateException("Failed to subscribe topic: " + topic, ex);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            mqttClient.unsubscribe(topic);
        }
        catch (MqttException ex) {
            throw new IllegalStateException("Failed to unsubscribe topic: " + topic, ex);
        }
    }

    @Override
    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 0, false);
    }

    @Override
    public void publish(String topic, byte[] payload, int qos, boolean retained) {
        try {
            mqttClient.publish(topic, toMessage(payload, qos, retained));
        }
        catch (MqttException ex) {
            throw new IllegalStateException("Failed to publish topic: " + topic, ex);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload) {
        return publishAsync(topic, payload, 0, false);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained) {
        return CompletableFuture.runAsync(() -> publish(topic, payload, qos, retained));
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload, Executor executor) {
        return publishAsync(topic, payload, 0, false, executor);
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained, Executor executor) {
        if (executor == null) throw new NullPointerException();
        return CompletableFuture.runAsync(() -> publish(topic, payload, qos, retained), executor);
    }

    @Override
    public boolean supportsManualAck() {
        return false;
    }

    private static String buildServerUri(MqttBrokerDefinition brokerDefinition) {
        return (brokerDefinition.isSslEnabled() ? "ssl://" : "tcp://") + brokerDefinition.getHost() + ":" + brokerDefinition.getPort();
    }

    private static MqttConnectOptions buildConnectOptions(MqttBrokerDefinition brokerDefinition) {
        MqttConnectOptions options = new MqttConnectOptions();
        if (brokerDefinition.getUsername() != null && !brokerDefinition.getUsername().isBlank()) {
            options.setUserName(brokerDefinition.getUsername());
        }
        if (brokerDefinition.getPassword() != null) {
            options.setPassword(brokerDefinition.getPassword().toCharArray());
        }
        options.setKeepAliveInterval(brokerDefinition.getKeepAliveInterval());
        options.setConnectionTimeout(brokerDefinition.getConnectionTimeout());
        options.setCleanSession(brokerDefinition.isCleanSession());
        options.setAutomaticReconnect(false);
        return options;
    }

    private static MqttMessage toMessage(byte[] payload, int qos, boolean retained) {
        if (payload == null) {
            throw new IllegalArgumentException("payload bytes must not be null");
        }
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        return message;
    }

    private void notifyConnected() {
        for (MqttConnectionListener listener : connectionListeners) {
            listener.onConnected(getBrokerId());
        }
    }

    private void notifyConnectionLost(Throwable cause) {
        for (MqttConnectionListener listener : connectionListeners) {
            listener.onConnectionLost(getBrokerId(), cause);
        }
    }

    private void notifyDisconnected() {
        for (MqttConnectionListener listener : connectionListeners) {
            listener.onDisconnected(getBrokerId());
        }
    }

    private final class CallbackHandler implements MqttCallbackExtended {
        @Override
        public void connectComplete(boolean reconnect, String connectedServerUri) {
            handleConnectComplete(reconnect, connectedServerUri);
        }

        @Override
        public void connectionLost(Throwable cause) {
            handleConnectionLost(cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            handleMessage(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
}
package io.github.mqttplus.integration;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.MqttHeaders;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SpringIntegrationMqttClientAdapter implements MqttClientAdapter {

    private final MqttBrokerDefinition brokerDefinition;
    private final MqttInboundMessageSink inboundMessageSink;
    private final DefaultMqttPahoClientFactory clientFactory;
    private final MqttPahoMessageDrivenChannelAdapter inboundAdapter;
    private final MqttPahoMessageHandler outboundHandler;
    private final List<MqttConnectionListener> connectionListeners = new ArrayList<>();
    private final String serverUri;

    public SpringIntegrationMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                                              MqttInboundMessageSink inboundMessageSink) {
        this(brokerDefinition,
                inboundMessageSink,
                buildClientFactory(brokerDefinition),
                buildServerUri(brokerDefinition));
    }

    SpringIntegrationMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                                       MqttInboundMessageSink inboundMessageSink,
                                       DefaultMqttPahoClientFactory clientFactory,
                                       String serverUri) {
        this(
                brokerDefinition,
                inboundMessageSink,
                clientFactory,
                buildInboundAdapter(serverUri, brokerDefinition, clientFactory),
                buildOutboundHandler(serverUri, brokerDefinition, clientFactory),
                serverUri);
    }

    SpringIntegrationMqttClientAdapter(MqttBrokerDefinition brokerDefinition,
                                       MqttInboundMessageSink inboundMessageSink,
                                       DefaultMqttPahoClientFactory clientFactory,
                                       MqttPahoMessageDrivenChannelAdapter inboundAdapter,
                                       MqttPahoMessageHandler outboundHandler,
                                       String serverUri) {
        this.brokerDefinition = Objects.requireNonNull(brokerDefinition, "brokerDefinition must not be null");
        this.inboundMessageSink = Objects.requireNonNull(inboundMessageSink, "inboundMessageSink must not be null");
        this.clientFactory = Objects.requireNonNull(clientFactory, "clientFactory must not be null");
        this.inboundAdapter = Objects.requireNonNull(inboundAdapter, "inboundAdapter must not be null");
        this.outboundHandler = Objects.requireNonNull(outboundHandler, "outboundHandler must not be null");
        this.serverUri = Objects.requireNonNull(serverUri, "serverUri must not be null");
        DirectChannel inboundChannel = new DirectChannel();
        inboundChannel.subscribe(this::handleInboundMessage);
        this.inboundAdapter.setOutputChannel(inboundChannel);
        initializeComponent(this.inboundAdapter, "inbound adapter");
        initializeComponent(this.outboundHandler, "outbound handler");
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
            outboundHandler.start();
            inboundAdapter.start();
            notifyConnected();
        }
        catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to connect broker: " + brokerDefinition.getBrokerId(), ex);
        }
    }

    @Override
    public void disconnect() {
        try {
            inboundAdapter.stop();
            outboundHandler.stop();
            notifyDisconnected();
        }
        catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to disconnect broker: " + brokerDefinition.getBrokerId(), ex);
        }
    }

    @Override
    public void subscribe(String topic, int qos) {
        try {
            inboundAdapter.addTopic(topic, qos);
        }
        catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to subscribe topic: " + topic, ex);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            inboundAdapter.removeTopic(topic);
        }
        catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to unsubscribe topic: " + topic, ex);
        }
    }

    @Override
    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 0, false);
    }

    @Override
    public void publish(String topic, byte[] payload, int qos, boolean retained) {
        if (payload == null) {
            throw new IllegalArgumentException("payload bytes must not be null");
        }
        try {
            outboundHandler.handleMessage(org.springframework.messaging.support.MessageBuilder.withPayload(payload)
                    .setHeader(org.springframework.integration.mqtt.support.MqttHeaders.TOPIC, topic)
                    .setHeader(org.springframework.integration.mqtt.support.MqttHeaders.QOS, qos)
                    .setHeader(org.springframework.integration.mqtt.support.MqttHeaders.RETAINED, retained)
                    .build());
        }
        catch (RuntimeException ex) {
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

    String getServerUri() {
        return serverUri;
    }

    DefaultMqttPahoClientFactory getClientFactory() {
        return clientFactory;
    }

    void handleInboundMessage(Message<?> message) {
        byte[] payload = toPayload(message.getPayload());
        Map<String, Object> headers = new LinkedHashMap<>();
        copyHeader(message, headers, org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_TOPIC, "topic");
        copyHeader(message, headers, org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_QOS, "qos");
        copyHeader(message, headers, org.springframework.integration.mqtt.support.MqttHeaders.RECEIVED_RETAINED, "retained");
        String topic = (String) headers.getOrDefault("topic", "");
        inboundMessageSink.onMessage(getBrokerId(), topic, payload, headers.isEmpty() ? MqttHeaders.empty() : new MqttHeaders(headers));
    }

    private static void copyHeader(Message<?> message, Map<String, Object> target, String sourceHeader, String targetHeader) {
        Object value = message.getHeaders().get(sourceHeader);
        if (value != null) {
            target.put(targetHeader, value);
        }
    }

    private static void initializeComponent(Object component, String name) {
        if (component instanceof InitializingBean initializingBean) {
            try {
                initializingBean.afterPropertiesSet();
            }
            catch (Exception ex) {
                throw new IllegalStateException("Failed to initialize " + name, ex);
            }
        }
    }

    private static DefaultMqttPahoClientFactory buildClientFactory(MqttBrokerDefinition brokerDefinition) {
        DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{buildServerUri(brokerDefinition)});
        if (brokerDefinition.getUsername() != null && !brokerDefinition.getUsername().isBlank()) {
            options.setUserName(brokerDefinition.getUsername());
        }
        if (brokerDefinition.getPassword() != null) {
            options.setPassword(brokerDefinition.getPassword().toCharArray());
        }
        options.setConnectionTimeout(brokerDefinition.getConnectionTimeout());
        options.setKeepAliveInterval(brokerDefinition.getKeepAliveInterval());
        options.setCleanSession(brokerDefinition.isCleanSession());
        options.setAutomaticReconnect(true);
        clientFactory.setConnectionOptions(options);
        return clientFactory;
    }

    private static MqttPahoMessageDrivenChannelAdapter buildInboundAdapter(String serverUri,
                                                                           MqttBrokerDefinition brokerDefinition,
                                                                           DefaultMqttPahoClientFactory clientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(serverUri, brokerDefinition.getClientId(), clientFactory);
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("mqtt-plus-integration-");
        taskScheduler.initialize();
        adapter.setTaskScheduler(taskScheduler);
        adapter.setCompletionTimeout(5000);
        adapter.setRecoveryInterval(2000);
        return adapter;
    }

    private static MqttPahoMessageHandler buildOutboundHandler(String serverUri,
                                                               MqttBrokerDefinition brokerDefinition,
                                                               DefaultMqttPahoClientFactory clientFactory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(serverUri, brokerDefinition.getClientId() + "-outbound", clientFactory);
        handler.setAsync(false);
        return handler;
    }

    private static String buildServerUri(MqttBrokerDefinition brokerDefinition) {
        return (brokerDefinition.isSslEnabled() ? "ssl://" : "tcp://")
                + brokerDefinition.getHost()
                + ":"
                + brokerDefinition.getPort();
    }

    private static byte[] toPayload(Object payload) {
        if (payload instanceof byte[] bytes) {
            return bytes;
        }
        if (payload instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("unsupported inbound payload type: " + payload.getClass().getName());
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
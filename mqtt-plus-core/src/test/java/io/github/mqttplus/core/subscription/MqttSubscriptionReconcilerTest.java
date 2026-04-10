package io.github.mqttplus.core.subscription;

import io.github.mqttplus.core.adapter.DefaultMqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.MqttListenerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import io.github.mqttplus.core.router.MqttListenerRegistry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MqttSubscriptionReconcilerTest {

    @Test
    void shouldReplayStaticAndDynamicSubscriptionsOnConnect() throws NoSuchMethodException {
        DefaultMqttClientAdapterRegistry adapterRegistry = new DefaultMqttClientAdapterRegistry();
        RecordingAdapter adapter = new RecordingAdapter("primary");
        adapterRegistry.register(adapter);
        MqttListenerRegistry listenerRegistry = new MqttListenerRegistry();
        Method method = SampleHandler.class.getDeclaredMethod("handle", String.class);
        listenerRegistry.register(new MqttListenerDefinition("handler", new SampleHandler(), method, "*", List.of("devices/+/status"), 1, String.class));
        DefaultMqttSubscriptionManager subscriptionManager = new DefaultMqttSubscriptionManager();
        subscriptionManager.addSubscription("primary", "dynamic/topic", 0);
        MqttSubscriptionReconciler reconciler = new MqttSubscriptionReconciler(adapterRegistry, listenerRegistry, subscriptionManager);

        reconciler.onConnected("primary");

        assertEquals(List.of("devices/+/status@1", "dynamic/topic@0"), adapter.subscriptions);
    }

    static final class SampleHandler {
        void handle(String payload) {
        }
    }

    static final class RecordingAdapter implements MqttClientAdapter {
        private final MqttBrokerDefinition definition;
        private final java.util.List<String> subscriptions = new java.util.ArrayList<>();

        RecordingAdapter(String brokerId) {
            this.definition = MqttBrokerDefinition.builder()
                    .brokerId(brokerId)
                    .host("127.0.0.1")
                    .clientId("recorder")
                    .inboundThreadPool(ThreadPoolConfig.builder().build())
                    .build();
        }

        @Override
        public String getBrokerId() {
            return definition.getBrokerId();
        }

        @Override
        public MqttBrokerDefinition getBrokerDefinition() {
            return definition;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void subscribe(String topic, int qos) {
            subscriptions.add(topic + "@" + qos);
        }

        @Override
        public void unsubscribe(String topic) {
        }

        @Override
        public void publish(String topic, byte[] payload) {
            publish(topic, payload, 0, false);
        }

        @Override
        public void publish(String topic, byte[] payload, int qos, boolean retained) {
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload) {
            return publishAsync(topic, payload, 0, false);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, Executor executor) {
            return publishAsync(topic, payload, 0, false, executor);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained, Executor executor) {
            return CompletableFuture.runAsync(() -> {}, executor);
        }

        @Override
        public boolean supportsManualAck() {
            return false;
        }

        @Override
        public void addConnectionListener(MqttConnectionListener listener) {
        }
    }
}
package io.github.mqttplus.spring.event;

import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.subscription.MqttSubscriptionManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class MqttSubscriptionRefreshEventListenerTest {

    @Test
    void shouldBridgeSubscribeAndUnsubscribeEvents() {
        MqttSubscriptionManager subscriptionManager = Mockito.mock(MqttSubscriptionManager.class);
        MqttClientAdapter adapter = Mockito.mock(MqttClientAdapter.class);
        MqttClientAdapterRegistry adapterRegistry = new StubRegistry(adapter);
        MqttSubscriptionRefreshEventListener listener = new MqttSubscriptionRefreshEventListener(subscriptionManager, adapterRegistry);

        listener.onApplicationEvent(new MqttSubscriptionRefreshEvent(MqttSubscriptionRefreshEvent.Action.SUBSCRIBE, "primary", "devices/1/status", 1));
        listener.onApplicationEvent(new MqttSubscriptionRefreshEvent(MqttSubscriptionRefreshEvent.Action.UNSUBSCRIBE, "primary", "devices/1/status", 0));

        Mockito.verify(subscriptionManager).addSubscription("primary", "devices/1/status", 1);
        Mockito.verify(subscriptionManager).removeSubscription("primary", "devices/1/status");
        Mockito.verify(adapter).subscribe("devices/1/status", 1);
        Mockito.verify(adapter).unsubscribe("devices/1/status");
    }

    private static final class StubRegistry implements MqttClientAdapterRegistry {
        private final MqttClientAdapter adapter;

        private StubRegistry(MqttClientAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void register(MqttClientAdapter adapter) {
        }

        @Override
        public Optional<MqttClientAdapter> find(String brokerId) {
            return Optional.of(adapter);
        }

        @Override
        public Collection<MqttClientAdapter> getAll() {
            return java.util.List.of(adapter);
        }

        @Override
        public void remove(String brokerId) {
        }
    }
}
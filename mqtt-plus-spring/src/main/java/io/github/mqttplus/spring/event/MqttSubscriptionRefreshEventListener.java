package io.github.mqttplus.spring.event;

import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.subscription.MqttSubscriptionManager;
import org.springframework.context.event.EventListener;

public final class MqttSubscriptionRefreshEventListener {

    private final MqttSubscriptionManager subscriptionManager;
    private final MqttClientAdapterRegistry adapterRegistry;

    public MqttSubscriptionRefreshEventListener(MqttSubscriptionManager subscriptionManager,
                                                MqttClientAdapterRegistry adapterRegistry) {
        this.subscriptionManager = subscriptionManager;
        this.adapterRegistry = adapterRegistry;
    }

    @EventListener
    public void onApplicationEvent(MqttSubscriptionRefreshEvent event) {
        if (event.action() == MqttSubscriptionRefreshEvent.Action.SUBSCRIBE) {
            subscriptionManager.addSubscription(event.brokerId(), event.topic(), event.qos());
            adapterRegistry.find(event.brokerId())
                    .ifPresent(adapter -> adapter.subscribe(event.topic(), event.qos()));
            return;
        }
        subscriptionManager.removeSubscription(event.brokerId(), event.topic());
        adapterRegistry.find(event.brokerId())
                .ifPresent(adapter -> adapter.unsubscribe(event.topic()));
    }
}
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
        if (event.getAction() == MqttSubscriptionRefreshEvent.Action.SUBSCRIBE) {
            subscriptionManager.addSubscription(event.getBrokerId(), event.getTopic(), event.getQos());
            adapterRegistry.find(event.getBrokerId())
                    .ifPresent(adapter -> adapter.subscribe(event.getTopic(), event.getQos()));
            return;
        }
        subscriptionManager.removeSubscription(event.getBrokerId(), event.getTopic());
        adapterRegistry.find(event.getBrokerId())
                .ifPresent(adapter -> adapter.unsubscribe(event.getTopic()));
    }
}
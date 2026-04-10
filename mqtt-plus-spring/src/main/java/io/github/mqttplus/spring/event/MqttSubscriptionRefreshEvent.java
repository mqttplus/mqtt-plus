package io.github.mqttplus.spring.event;

public record MqttSubscriptionRefreshEvent(Action action,
                                           String brokerId, String topic, int qos) {

    public enum Action {
        SUBSCRIBE,
        UNSUBSCRIBE
    }

}

package io.github.mqttplus.spring.event;

public final class MqttSubscriptionRefreshEvent {

    public enum Action {
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    private final Action action;
    private final String brokerId;
    private final String topic;
    private final int qos;

    public MqttSubscriptionRefreshEvent(Action action, String brokerId, String topic, int qos) {
        this.action = action;
        this.brokerId = brokerId;
        this.topic = topic;
        this.qos = qos;
    }

    public Action getAction() {
        return action;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public String getTopic() {
        return topic;
    }

    public int getQos() {
        return qos;
    }
}

# sample-dynamic-subscription

Spring Boot Web sample exposing HTTP endpoints that publish `MqttSubscriptionRefreshEvent` events.

Start the app, then call:

- `POST /subscriptions/subscribe?topic=your/topic`
- `POST /subscriptions/unsubscribe?topic=your/topic`
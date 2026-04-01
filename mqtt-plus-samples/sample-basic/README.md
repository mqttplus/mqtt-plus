# sample-basic

Minimal Spring Boot example with one broker and one `@MqttListener`.

By default, the app only starts the context. To publish a demo message on startup, run with:

```properties
samples.publish-on-startup=true
```
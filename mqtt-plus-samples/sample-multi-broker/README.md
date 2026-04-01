# sample-multi-broker

Spring Boot sample showing two brokers, wildcard broker listener registration, and explicit publish routing.

By default, the app only starts the context. To publish demo messages on startup, run with:

```properties
samples.publish-on-startup=true
```
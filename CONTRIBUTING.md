# Contributing

Thanks for contributing to `mqtt-plus`.

## Development

1. Create a feature branch from `main`.
2. Run local checks before committing:

```bash
mvn test
mvn -pl mqtt-plus-samples/sample-basic,mqtt-plus-samples/sample-multi-broker,mqtt-plus-samples/sample-dynamic-subscription -am -DskipTests compile
```

3. Keep changes scoped to one logical concern when possible.
4. Merge completed work back into `main`.

## Notes

- Java version: 17
- Current adapter in `v1.0.0`: Paho (MQTT 3.1.1)
- `mqtt-plus-test` provides fast router-level test helpers and Spring test wiring

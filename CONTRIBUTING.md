# Contributing

Thanks for contributing to `mqtt-plus`.

## Development

1. Create a feature branch from `main`.
2. Run local checks before committing:

```bash
mvn -B test
mvn -B -Pintegration-test verify
```

3. Keep changes scoped to one logical concern when possible.
4. Merge completed work back into `main`.

## Notes

- Java version: 17
- Current adapter in `v1.0.0`: Paho (MQTT 3.1.1)
- `mqtt-plus-test` provides router-level fast tests and embedded-broker Spring test support
- The three sample applications are expected to pass their smoke tests as part of release readiness
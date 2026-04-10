<div align="center">

# mqtt-plus

**Annotation-driven MQTT framework for Spring Boot**

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net)
[![MQTT](https://img.shields.io/badge/MQTT-3.1.1-green.svg)](https://mqtt.org)

[English](#english) | [中文](#中文)

</div>

---

<a name="english"></a>

## English

### Why mqtt-plus?

Using MQTT in Spring Boot often means wiring channels, adapters, and handlers even for simple topic consumption. `mqtt-plus` keeps the application model centered on annotations and explicit broker publishing, while adapter internals handle transport concerns such as reconnects, runtime subscriptions, and session options.

```java
@MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
public void onStatus(String payload) {
    System.out.println(payload);
}
```

### Current Scope

This README reflects the `v1.1.0` release scope:

- Included: `mqtt-plus-core`, `mqtt-plus-paho`, `mqtt-plus-spring-integration`, `mqtt-plus-spring`, `mqtt-plus-spring-boot-starter`, `mqtt-plus-test`
- Deferred: `mqtt-plus-hivemq`, MQTT 5.0 support, runtime broker connection reconfiguration

### Features

- `@MqttListener` with MQTT wildcard support (`+`, `#`)
- Multi-broker configuration in one application
- Dynamic subscribe / unsubscribe applied immediately to active adapters
- Subscription recovery after reconnect
- `MqttTemplate` sync / async publish with `qos` and `retained`
- Spring Boot starter with per-broker adapter selection
- Embedded-broker test support via `@EnableMqttPlusTest`
- Non-Spring capable core plus raw Paho fallback adapter

### Quick Start

**1. Add dependencies**

For Spring Boot applications in `v1.1.0`, use the starter and the Spring Integration adapter:

```xml
<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>

<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-spring-integration</artifactId>
    <version>1.1.0</version>
</dependency>
```

If you prefer the raw Paho transport instead, replace the second dependency with `mqtt-plus-paho`.

**JSON payload note**

- `String` payloads are sent as UTF-8 bytes
- `byte[]` payloads are sent as-is
- POJO payloads are serialized through the publish-side `PayloadSerializer` chain
- When `jackson-databind` is on the classpath, the starter auto-enables a Jackson serializer so POJOs are published as JSON bytes by default
- You can register a custom `PayloadSerializer` for another JSON framework or a custom wire format

**2. Configure brokers**

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      client-id: my-app-001
      mqtt-version: 3.1.1
      clean-session: false
```

**3. Listen and publish**

```java
@Component
public class DroneMessageHandler {

    private final MqttTemplate mqttTemplate;

    public DroneMessageHandler(MqttTemplate mqttTemplate) {
        this.mqttTemplate = mqttTemplate;
    }

    @MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
    public void onStatus(String payload, MqttHeaders headers) {
        System.out.println("Payload: " + payload);
        System.out.println("Topic: " + headers.getTopic());
    }

    public void sendCommand(String sn, String cmd) {
        mqttTemplate.publishAsync("cloud", "drone/" + sn + "/command", cmd, 1, false);
    }
}
```

### Adapter Selection

By default, Spring Boot apps prefer `spring-integration` when it is on the classpath and compatible with the broker MQTT version.

You can override the adapter explicitly per broker:

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      mqtt-version: 3.1.1
      adapter: paho
```

Supported selection model:

- `adapter`: transport identity such as `spring-integration` or `paho`
- `mqtt-version`: protocol compatibility such as `3.1.1`
- `clean-session`: session behavior passed to the adapter transport

### Dynamic Subscription

```java
publisher.publishEvent(new MqttSubscriptionRefreshEvent(
        MqttSubscriptionRefreshEvent.Action.SUBSCRIBE,
        "cloud",
        "drone/" + newSn + "/status",
        0
));
```

Dynamic subscription refresh events are applied immediately to the active adapter and are also retained for future reconnect recovery.

### Testing

`mqtt-plus-test` supports two complementary styles:

- `MqttTestTemplate.simulateIncoming(...)` for fast router-level tests
- `@EnableMqttPlusTest` for Spring tests backed by an embedded MQTT broker

### Samples

The repository includes three Spring Boot sample applications:

- `sample-basic`
- `sample-multi-broker`
- `sample-dynamic-subscription`

All three samples are covered by smoke tests in CI.

### Modules

| Module | Purpose |
|------|------|
| `mqtt-plus-core` | Core abstractions, routing, reconciliation, and SPI |
| `mqtt-plus-paho` | Raw Eclipse Paho adapter for MQTT 3.1.1 |
| `mqtt-plus-spring-integration` | Spring Integration-based adapter recommended for Spring Boot |
| `mqtt-plus-spring` | Annotation scanning, method resolution, and event bridging |
| `mqtt-plus-spring-boot-starter` | Auto-configuration, YAML binding, adapter selection |
| `mqtt-plus-test` | Router-level and embedded-broker testing support |

### Comparison

| Feature | mqtt-plus | spring-integration-mqtt | Paho (raw) |
|---------|:---------:|:----------------------:|:----------:|
| Annotation-driven listeners | ✅ | ❌ | ❌ |
| Multi-broker | ✅ | ⚠️ | ❌ |
| Dynamic subscriptions | ✅ | ⚠️ | ⚠️ |
| MQTT 5.0 | ⚠️ | ❌ | ⚠️ |
| Spring Boot Starter | ✅ | ❌ | ❌ |
| Non-Spring usage | ✅ | ❌ | ✅ |
| Interceptor chain | ✅ | ❌ | ❌ |
| Async publish | ✅ | ⚠️ | ⚠️ |
| Test helper module | ✅ | ❌ | ❌ |

### Notes

- `MqttTemplate` requires an explicit broker id for publishing
- `MqttTestTemplate.simulateIncoming(...)` is a fast router-level testing utility, not a full protocol simulator
- Runtime broker connection reconfiguration is outside the current scope
- Multiple listeners can match the same topic and will all be invoked
- Listener invocation still depends on payload conversion: a String listener can consume plain text, while a typed listener such as DroneStatus requires payload bytes that can be deserialized into that target type (for example JSON when using Jackson)

### Requirements

- Java 17+
- Spring Boot 2.7+

### License

Apache 2.0

---

<a name="中文"></a>

## 中文

### 为什么选择 mqtt-plus？

在 Spring Boot 中使用 MQTT，即使只是监听一个简单 topic，通常也需要自己组装 channel、adapter 和 handler。`mqtt-plus` 的目标是把应用开发模型稳定在注解、显式 broker 发布和清晰的订阅管理上，把 transport 层的重连、运行时订阅和会话选项封装在 adapter 内部。

```java
@MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
public void onStatus(String payload) {
    System.out.println(payload);
}
```

### 当前范围

本 README 对应 `v1.1.0` 正式发布范围：

- 已包含：`mqtt-plus-core`、`mqtt-plus-paho`、`mqtt-plus-spring-integration`、`mqtt-plus-spring`、`mqtt-plus-spring-boot-starter`、`mqtt-plus-test`
- 暂缓：`mqtt-plus-hivemq`、MQTT 5.0 支持、运行时动态修改 broker 连接信息

### 核心能力

- `@MqttListener` 注解驱动监听，支持 `+`、`#` 通配
- 单应用多 broker 配置
- 运行时动态订阅 / 取消订阅，并立即应用到活跃 adapter
- 重连后的订阅恢复
- `MqttTemplate` 支持同步 / 异步发布，以及 `qos`、`retained`
- starter 支持按 broker 选择 adapter
- `@EnableMqttPlusTest` 提供 embedded broker 测试支持
- 保留非 Spring 场景下的 core + Paho 使用能力

### 快速开始

**1. 添加依赖**

对于 `v1.1.0` 的 Spring Boot 应用，推荐使用 starter + Spring Integration adapter：

```xml
<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>

<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-spring-integration</artifactId>
    <version>1.1.0</version>
</dependency>
```

如果你更希望继续使用 raw Paho，可以把第二个依赖换成 `mqtt-plus-paho`。

**JSON 负载说明**

- `String` 默认按 UTF-8 bytes 发送
- `byte[]` 会原样发送
- Java POJO 会经过发送侧 `PayloadSerializer` 链进行序列化
- 当类路径里存在 `jackson-databind` 时，starter 会自动启用 Jackson serializer，因此 POJO 默认会按 JSON bytes 发布
- 如果你使用其他 JSON 框架或自定义线协议，可以注册自己的 `PayloadSerializer`

**2. 配置 broker**

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      client-id: my-app-001
      mqtt-version: 3.1.1
      clean-session: false
```

**3. 监听和发布**

```java
@Component
public class DroneMessageHandler {

    private final MqttTemplate mqttTemplate;

    public DroneMessageHandler(MqttTemplate mqttTemplate) {
        this.mqttTemplate = mqttTemplate;
    }

    @MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
    public void onStatus(String payload, MqttHeaders headers) {
        System.out.println("Payload: " + payload);
        System.out.println("Topic: " + headers.getTopic());
    }

    public void sendCommand(String sn, String cmd) {
        mqttTemplate.publishAsync("cloud", "drone/" + sn + "/command", cmd, 1, false);
    }
}
```

### Adapter 选择

对于 Spring Boot 应用，只要类路径中存在并且协议兼容，starter 默认优先选择 `spring-integration`。

你也可以按 broker 显式覆盖：

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      mqtt-version: 3.1.1
      adapter: paho
```

当前选择模型中：

- `adapter`：transport 身份，例如 `spring-integration`、`paho`
- `mqtt-version`：协议兼容性，例如 `3.1.1`
- `clean-session`：传递给 adapter transport 的会话行为

### 动态订阅

```java
publisher.publishEvent(new MqttSubscriptionRefreshEvent(
        MqttSubscriptionRefreshEvent.Action.SUBSCRIBE,
        "cloud",
        "drone/" + newSn + "/status",
        0
));
```

动态订阅刷新事件会立即作用到当前活跃 adapter，同时也会被保留用于后续重连恢复。

### 测试支持

`mqtt-plus-test` 提供两种互补的测试方式：

- `MqttTestTemplate.simulateIncoming(...)`：适合快速 router 级测试
- `@EnableMqttPlusTest`：适合带 embedded MQTT broker 的 Spring 测试

### 示例工程

仓库当前包含 3 个 Spring Boot 示例：

- `sample-basic`
- `sample-multi-broker`
- `sample-dynamic-subscription`

这 3 个 sample 都已经纳入 CI smoke test。

### 模块说明

| 模块 | 作用 |
|------|------|
| `mqtt-plus-core` | 核心抽象、路由、订阅协调与 SPI |
| `mqtt-plus-paho` | 基于 Eclipse Paho 的 MQTT 3.1.1 原生 adapter |
| `mqtt-plus-spring-integration` | 基于 Spring Integration 的 Spring Boot 推荐 adapter |
| `mqtt-plus-spring` | 注解扫描、方法参数解析、事件桥接 |
| `mqtt-plus-spring-boot-starter` | 自动配置、YAML 绑定、adapter 选择 |
| `mqtt-plus-test` | router 级测试和 embedded broker 测试支持 |

### 对比

| 功能 | mqtt-plus | spring-integration-mqtt | Paho 原生 |
|------|:---------:|:----------------------:|:---------:|
| 注解驱动监听 | ✅ | ❌ | ❌ |
| 多 broker | ✅ | ⚠️ | ❌ |
| 动态订阅 | ✅ | ⚠️ | ⚠️ |
| MQTT 5.0 | ⚠️ | ❌ | ⚠️ |
| Spring Boot Starter | ✅ | ❌ | ❌ |
| 非 Spring 使用 | ✅ | ❌ | ✅ |
| 拦截器链 | ✅ | ❌ | ❌ |
| 异步发布 | ✅ | ⚠️ | ⚠️ |
| 测试辅助模块 | ✅ | ❌ | ❌ |

### 说明

- `MqttTemplate` 发布时必须显式指定 broker id
- `MqttTestTemplate.simulateIncoming(...)` 是 router 级快速测试工具，不是完整协议模拟器
- 运行时动态修改 broker 连接参数不在当前范围内

### 环境要求

- Java 17+
- Spring Boot 2.7+

### License

Apache 2.0

<div align="center">

如果这个项目对你有帮助，欢迎 ⭐ Star！

If this project helps you, please consider giving it a ⭐ Star!

</div>

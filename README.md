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

Using MQTT in Spring Boot often means wiring channels, adapters, and handlers for even simple topic consumption. `mqtt-plus` aims to provide a cleaner model centered on annotations, explicit broker publishing, and subscription recovery.

```java
@MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
public void onStatus(String payload) {
    System.out.println(payload);
}
```

### Current Scope

This README reflects the currently implemented and reviewed `v1.0.0` scope:

- Included: `mqtt-plus-core`, `mqtt-plus-paho`, `mqtt-plus-spring`, `mqtt-plus-spring-boot-starter`, `mqtt-plus-test`
- Deferred: `mqtt-plus-hivemq`, MQTT 5.0 support, dynamic broker connection reconfiguration

### Features

- `@MqttListener`: annotation-driven listener registration with MQTT wildcard support (`+`, `#`)
- Multi-broker: connect to multiple MQTT brokers in one application
- Dynamic subscriptions: add or remove topics at runtime
- Reconnect recovery: restore static and dynamic subscriptions after reconnect
- `MqttTemplate`: explicit-broker publish API with sync and async variants
- Interceptor chain: `beforeHandle` / `afterHandle` / `onError`
- Spring Boot first, non-Spring capable: core abstractions remain usable outside Spring

### Quick Start

**1. Add dependencies**

For `v1.0.0`, add both the starter and the Paho adapter explicitly:

```xml
<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-paho</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**JSON payload note**

- `mqtt-plus` always supports `String` and `byte[]` payloads out of the box
- POJO payload binding requires a JSON `PayloadConverter`
- If `jackson-databind` is present on the classpath, the starter auto-enables a Jackson-based converter
- If you prefer another JSON framework, register your own `PayloadConverter` bean instead
- `jackson-databind` is optional by design, so simple apps do not need it unless they deserialize JSON into objects

**2. Configure brokers**

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      clientId: my-app-001
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
        mqttTemplate.publishAsync(
                "cloud",
                "drone/" + sn + "/command",
                cmd,
                1,
                false
        );
    }
}
```

### Multi-Broker Example

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: mqtt.example.com
      port: 1883
      clientId: cloud-client-001
    local:
      host: 192.168.1.100
      port: 1883
      clientId: local-client-001
```

```java
@MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
public void onCloudStatus(String payload) {}

@MqttListener(broker = "*", topics = "alert/#", payloadType = String.class)
public void onAlert(String payload) {}

@MqttListener(
        broker = "cloud",
        topics = {"drone/+/status", "drone/+/heartbeat"},
        payloadType = String.class
)
public void onDroneMessage(String payload) {}
```

### Dynamic Subscription

```java
publisher.publishEvent(new MqttSubscriptionRefreshEvent(
        MqttSubscriptionRefreshEvent.Action.SUBSCRIBE,
        "cloud",
        "drone/" + newSn + "/status",
        0
));
```

`v1.0.0` supports dynamic topic subscription updates.  
It does not support changing broker connection details such as host, port, username, password, or clientId at runtime.

### Modules

| Module | Purpose |
|------|------|
| `mqtt-plus-core` | Pure Java core abstractions, routing, subscription reconciliation, and SPI |
| `mqtt-plus-paho` | Eclipse Paho v1 adapter for MQTT 3.1.1 |
| `mqtt-plus-spring` | Spring integration for annotation scanning, method resolution, and event bridging |
| `mqtt-plus-spring-boot-starter` | Auto-configuration, YAML binding, and default converter setup |
| `mqtt-plus-test` | Test helpers for router-level testing and Spring test wiring |

### Comparison

| Feature | mqtt-plus | spring-integration-mqtt | Paho (raw) |
|---------|:---------:|:----------------------:|:----------:|
| Annotation-driven listeners | ✅ | ❌ | ❌ |
| Multi-broker | ✅ | ⚠️ | ❌ |
| Dynamic subscriptions | ✅ | ⚠️ | ⚠️ |
| MQTT 5.0 | ✅ | ❌ | ⚠️ |
| Spring Boot Starter | ✅ | ❌ | ❌ |
| Non-Spring usage | ✅ | ❌ | ✅ |
| Interceptor chain | ✅ | ❌ | ❌ |
| Async publish | ✅ | ⚠️ | ⚠️ |
| Test helper module | ✅ | ❌ | ❌ |

### Notes

- `MqttTemplate` requires an explicit broker id for publishing
- `MqttTestTemplate.simulateIncoming(...)` is a fast router-level testing utility, not a full MQTT protocol simulator
- Runtime broker connection reconfiguration is outside the `v1.0.0` scope

### Requirements

- Java 17+
- Spring Boot 2.7+

### License

Apache 2.0

---

<a name="中文"></a>

## 中文

### 为什么是 mqtt-plus？

在 Spring Boot 中使用 MQTT，哪怕只是做一个简单的 topic 监听，往往也要手动组装 channel、adapter 和 handler。`mqtt-plus` 希望把这些样板配置收敛起来，提供一套以注解、显式 broker 发布和订阅恢复为中心的开发模型。

```java
@MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
public void onStatus(String payload) {
    System.out.println(payload);
}
```

### 当前范围

本 README 对应当前已经实现并评审收敛的 `v1.0.0` 范围：

- 已包含：`mqtt-plus-core`、`mqtt-plus-paho`、`mqtt-plus-spring`、`mqtt-plus-spring-boot-starter`、`mqtt-plus-test`
- 暂缓：`mqtt-plus-hivemq`、MQTT 5.0 支持、运行时动态修改 broker 连接信息

### 功能特性

- `@MqttListener`：注解驱动的监听注册，支持 MQTT 通配符（`+`、`#`）
- 多 broker：一个应用可以同时连接多个 MQTT broker
- 动态订阅：支持在运行时增加或移除 topic
- 重连恢复：连接恢复后自动恢复静态和动态订阅
- `MqttTemplate`：显式指定 broker 的同步/异步发布 API
- 拦截器链：支持 `beforeHandle` / `afterHandle` / `onError`
- 以 Spring Boot 为主，同时保留非 Spring 使用能力

### 快速开始

**1. 添加依赖**

在 `v1.0.0` 中，请显式同时引入 starter 和 Paho 适配器：

```xml
<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>io.github.mqttplus</groupId>
    <artifactId>mqtt-plus-paho</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**JSON 负载说明**

- `mqtt-plus` 默认始终支持 `String` 和 `byte[]` 负载
- 如果要把消息直接反序列化为 POJO，需要提供 JSON `PayloadConverter`
- 当类路径中存在 `jackson-databind` 时，starter 会自动启用基于 Jackson 的 converter
- 如果你更希望使用其他 JSON 框架，也可以自己注册 `PayloadConverter` Bean
- `jackson-databind` 是可选依赖，简单应用如果不做 JSON 对象反序列化，可以不引入

**2. 配置 broker**

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      clientId: my-app-001
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
        mqttTemplate.publishAsync(
                "cloud",
                "drone/" + sn + "/command",
                cmd,
                1,
                false
        );
    }
}
```

### 多 broker 示例

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: mqtt.example.com
      port: 1883
      clientId: cloud-client-001
    local:
      host: 192.168.1.100
      port: 1883
      clientId: local-client-001
```

```java
@MqttListener(broker = "cloud", topics = "drone/+/status", payloadType = String.class)
public void onCloudStatus(String payload) {}

@MqttListener(broker = "*", topics = "alert/#", payloadType = String.class)
public void onAlert(String payload) {}

@MqttListener(
        broker = "cloud",
        topics = {"drone/+/status", "drone/+/heartbeat"},
        payloadType = String.class
)
public void onDroneMessage(String payload) {}
```

### 动态订阅

```java
publisher.publishEvent(new MqttSubscriptionRefreshEvent(
        MqttSubscriptionRefreshEvent.Action.SUBSCRIBE,
        "cloud",
        "drone/" + newSn + "/status",
        0
));
```

`v1.0.0` 支持动态增删 topic。  
但暂不支持在运行时动态修改 broker 的 host、port、username、password 或 clientId。

### 模块说明

| 模块 | 说明 |
|------|------|
| `mqtt-plus-core` | 纯 Java 核心抽象、路由、订阅协调和 SPI |
| `mqtt-plus-paho` | 基于 Eclipse Paho v1 的 MQTT 3.1.1 适配器 |
| `mqtt-plus-spring` | Spring 注解扫描、方法参数解析和事件桥接 |
| `mqtt-plus-spring-boot-starter` | 自动配置、YAML 绑定和默认 converter 装配 |
| `mqtt-plus-test` | 用于 router 级测试和 Spring 测试装配的辅助模块 |

### 对比

| Feature | mqtt-plus | spring-integration-mqtt | Paho (raw) |
|---------|:---------:|:----------------------:|:----------:|
| Annotation-driven listeners | ✅ | ❌ | ❌ |
| Multi-broker | ✅ | ⚠️ | ❌ |
| Dynamic subscriptions | ✅ | ⚠️ | ⚠️ |
| MQTT 5.0 | ✅ | ❌ | ⚠️ |
| Spring Boot Starter | ✅ | ❌ | ❌ |
| Non-Spring usage | ✅ | ❌ | ✅ |
| Interceptor chain | ✅ | ❌ | ❌ |
| Async publish | ✅ | ⚠️ | ⚠️ |
| Test helper module | ✅ | ❌ | ❌ |

### 说明

- `MqttTemplate` 发布时必须显式指定 broker id
- `MqttTestTemplate.simulateIncoming(...)` 是 router 级快速测试工具，不是完整的 MQTT 协议仿真
- 运行时动态更新 broker 连接参数不在 `v1.0.0` 范围内

### 运行要求

- Java 17+
- Spring Boot 2.7+

### 许可证

Apache 2.0

---

<div align="center">

如果这个项目对你有帮助，欢迎 ⭐ Star！

If this project helps you, please consider giving it a ⭐ Star!

</div>

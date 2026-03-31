<div align="center">

# mqtt-plus

**注解驱动的 Spring Boot MQTT 框架 · Annotation-driven MQTT framework for Spring Boot**

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://adoptium.net)
[![MQTT](https://img.shields.io/badge/MQTT-3.1.1-green.svg)](https://mqtt.org)

[English](#english) · [中文](#中文)

</div>

---

<a name="english"></a>

## English

### Why mqtt-plus?

Using MQTT in Spring Boot often means wiring channels, adapters, and handlers for even simple topic consumption. `mqtt-plus` aims to provide a cleaner model centered on annotations, explicit broker publishing, and subscription recovery.

```java
@MqttListener(topic = "drone/+/status", broker = "cloud")
public void onStatus(DroneStatusEvent event) {
    System.out.println("Drone: " + event.getDroneSn());
}
```

### Current Scope

This README reflects the currently reviewed `v1.0.0` scope:

- Included: `mqtt-plus-core`, `mqtt-plus-paho`, `mqtt-plus-spring`, `mqtt-plus-spring-boot-starter`, `mqtt-plus-test`
- Deferred: `mqtt-plus-hivemq`, MQTT 5.0 support, dynamic broker connection reconfiguration

### Features

- **`@MqttListener`**: annotation-driven listener registration with MQTT wildcard support (`+`, `#`)
- **Multi-broker**: connect to multiple MQTT brokers in one application
- **Dynamic subscriptions**: add or remove topics at runtime
- **Reconnect recovery**: restore static and dynamic subscriptions after reconnect
- **`MqttTemplate`**: explicit-broker publish API with sync and async variants
- **Interceptor chain**: `beforeHandle` / `afterHandle` / `onError`
- **Spring Boot first, non-Spring capable**: core abstractions remain usable outside Spring

### Quick Start

**1. Add dependencies**

For `v1.0.0`, add both the starter and the Paho adapter explicitly:

```xml
<dependency>
    <groupId>io.github.your-org</groupId>
    <artifactId>mqtt-plus-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>io.github.your-org</groupId>
    <artifactId>mqtt-plus-paho</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**2. Configure brokers**

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      client-id: my-app-001
```

**3. Listen and publish**

```java
@Component
public class DroneMessageHandler {

    @MqttListener(topic = "drone/+/status", broker = "cloud")
    public void onStatus(DroneStatusEvent event, MqttHeaders headers) {
        System.out.println("Topic: " + headers.getTopic());
        System.out.println("Drone: " + event.getDroneSn());
    }

    @Autowired
    private MqttTemplate mqttTemplate;

    public void sendCommand(String sn, String cmd) {
        mqttTemplate.publishAsync(
            "cloud",
            "drone/" + sn + "/command",
            new CommandEvent(cmd)
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
      client-id: cloud-client-001
    local:
      host: 192.168.1.100
      port: 1883
      client-id: local-client-001
```

```java
@MqttListener(topic = "drone/+/status", broker = "cloud")
public void onCloudStatus(DroneStatusEvent event) {}

@MqttListener(topic = "alert/#", broker = "*")
public void onAlert(AlertEvent event) {}

@MqttListener(topic = {"drone/+/status", "drone/+/heartbeat"}, broker = "cloud")
public void onDroneMessage(DroneEvent event) {}
```

### Dynamic Subscriptions

```java
applicationEventPublisher.publishEvent(
    MqttSubscriptionRefreshEvent.builder()
        .brokerId("cloud")
        .addTopics(Set.of("drone/" + newSn + "/status"))
        .removeTopics(Set.of("drone/" + oldSn + "/status"))
        .build()
);
```

Dynamic topic subscription is supported in `v1.0.0`. Dynamic broker connection changes are not.

### Module Structure

| Module | Description |
|--------|-------------|
| `mqtt-plus-core` | Pure Java core: interfaces, annotations, routing, error handling, subscription reconciliation |
| `mqtt-plus-paho` | MQTT 3.1.1 adapter via Eclipse Paho v1 |
| `mqtt-plus-spring` | Spring integration: annotation scanning, argument binding, event bridging |
| `mqtt-plus-spring-boot-starter` | Auto-configuration, YAML binding, built-in converters |
| `mqtt-plus-test` | Test helpers for fast router-level tests and embedded-test support |

### Notes

- `MqttTemplate` requires an explicit broker identifier when publishing
- `MqttTestTemplate.simulateIncoming(...)` is a fast test helper, not a full MQTT protocol substitute
- Runtime updates of broker host, port, username, password, or client ID are out of scope for `v1.0.0`

### Requirements

- Java 17+
- Spring Boot 2.7+

### License

Apache 2.0

---

<a name="中文"></a>

## 中文

### 为什么选择 mqtt-plus？

在 Spring Boot 中使用 MQTT 时，即使只是监听一个简单的 topic，也经常需要编写大量 channel、adapter 和 handler 样板代码。`mqtt-plus` 希望围绕注解监听、显式 broker 发布和订阅恢复，提供一套更简洁的使用方式。

```java
@MqttListener(topic = "drone/+/status", broker = "cloud")
public void onStatus(DroneStatusEvent event) {
    System.out.println("无人机：" + event.getDroneSn());
}
```

### 当前范围

本 README 对应当前评审收敛后的 `v1.0.0` 范围：

- 已纳入：`mqtt-plus-core`、`mqtt-plus-paho`、`mqtt-plus-spring`、`mqtt-plus-spring-boot-starter`、`mqtt-plus-test`
- 暂不纳入：`mqtt-plus-hivemq`、MQTT 5.0 支持、broker 连接信息运行时动态变更

### 核心能力

- **`@MqttListener`**：基于注解的监听注册，支持 MQTT 通配符（`+`、`#`）
- **多 Broker**：同一个应用可连接多个 MQTT Broker
- **动态订阅**：支持运行时新增或移除 topic
- **重连恢复**：断线重连后自动恢复静态与动态订阅
- **`MqttTemplate`**：显式 broker 的同步/异步发布 API
- **拦截器链**：支持 `beforeHandle` / `afterHandle` / `onError`
- **Spring Boot 优先，非 Spring 可用**：核心抽象在非 Spring 环境下也可使用

### 快速开始

**1. 添加依赖**

对于 `v1.0.0`，需要显式同时引入 starter 和 Paho 适配器：

```xml
<dependency>
    <groupId>io.github.your-org</groupId>
    <artifactId>mqtt-plus-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>io.github.your-org</groupId>
    <artifactId>mqtt-plus-paho</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**2. 配置 Broker**

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: broker.example.com
      port: 1883
      client-id: my-app-001
```

**3. 监听与发布**

```java
@Component
public class DroneMessageHandler {

    @MqttListener(topic = "drone/+/status", broker = "cloud")
    public void onStatus(DroneStatusEvent event, MqttHeaders headers) {
        System.out.println("Topic: " + headers.getTopic());
        System.out.println("无人机：" + event.getDroneSn());
    }

    @Autowired
    private MqttTemplate mqttTemplate;

    public void sendCommand(String sn, String cmd) {
        mqttTemplate.publishAsync(
            "cloud",
            "drone/" + sn + "/command",
            new CommandEvent(cmd)
        );
    }
}
```

### 多 Broker 示例

```yaml
mqtt-plus:
  brokers:
    cloud:
      host: mqtt.example.com
      port: 1883
      client-id: cloud-client-001
    local:
      host: 192.168.1.100
      port: 1883
      client-id: local-client-001
```

```java
@MqttListener(topic = "drone/+/status", broker = "cloud")
public void onCloudStatus(DroneStatusEvent event) {}

@MqttListener(topic = "alert/#", broker = "*")
public void onAlert(AlertEvent event) {}

@MqttListener(topic = {"drone/+/status", "drone/+/heartbeat"}, broker = "cloud")
public void onDroneMessage(DroneEvent event) {}
```

### 动态订阅

```java
applicationEventPublisher.publishEvent(
    MqttSubscriptionRefreshEvent.builder()
        .brokerId("cloud")
        .addTopics(Set.of("drone/" + newSn + "/status"))
        .removeTopics(Set.of("drone/" + oldSn + "/status"))
        .build()
);
```

`v1.0.0` 支持动态 topic 订阅。  
不支持运行时动态修改 broker 连接参数。

### 模块说明

| 模块 | 说明 |
|------|------|
| `mqtt-plus-core` | 纯 Java 核心：接口、注解、路由、错误处理、订阅协调 |
| `mqtt-plus-paho` | 基于 Eclipse Paho v1 的 MQTT 3.1.1 适配器 |
| `mqtt-plus-spring` | Spring 集成：注解扫描、参数绑定、事件桥接 |
| `mqtt-plus-spring-boot-starter` | 自动配置、YAML 绑定、内置转换器 |
| `mqtt-plus-test` | 用于快速 router 级测试和嵌入式测试支持的辅助模块 |

### 说明

- `MqttTemplate` 发布时必须显式指定 broker
- `MqttTestTemplate.simulateIncoming(...)` 是快速测试工具，不等同于完整 MQTT 协议仿真
- 运行时修改 broker 的 host、port、username、password、client ID 不在 `v1.0.0` 范围内

### 环境要求

- Java 17+
- Spring Boot 2.7+

### 许可证

Apache 2.0

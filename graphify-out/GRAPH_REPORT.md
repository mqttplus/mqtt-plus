# Graph Report - mqtt-plus  (2026-04-28)

## Corpus Check
- 102 files · ~16,621 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 820 nodes · 1506 edges · 37 communities detected
- Extraction: 63% EXTRACTED · 37% INFERRED · 0% AMBIGUOUS · INFERRED: 557 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]

## God Nodes (most connected - your core abstractions)
1. `BrokerProperties` - 32 edges
2. `MqttPlusAutoConfiguration` - 28 edges
3. `PahoMqttClientAdapter` - 25 edges
4. `SpringIntegrationMqttClientAdapter` - 25 edges
5. `Builder` - 23 edges
6. `MqttBrokerDefinition` - 15 edges
7. `Builder` - 13 edges
8. `RecordingAdapter` - 13 edges
9. `TestAdapter` - 13 edges
10. `RecordingAdapter` - 13 edges

## Surprising Connections (you probably didn't know these)
- `DefaultMqttTemplate` --implements--> `MqttTemplate`  [EXTRACTED]
  mqtt-plus-core\src\main\java\io\github\mqttplus\core\DefaultMqttTemplate.java →   _Bridges community 11 → community 0_
- `DefaultMqttClientAdapterRegistry` --implements--> `MqttClientAdapterRegistry`  [EXTRACTED]
  mqtt-plus-core\src\main\java\io\github\mqttplus\core\adapter\DefaultMqttClientAdapterRegistry.java →   _Bridges community 5 → community 0_
- `StubRegistry` --implements--> `MqttClientAdapterRegistry`  [EXTRACTED]
  mqtt-plus-spring\src\test\java\io\github\mqttplus\spring\event\MqttSubscriptionRefreshEventListenerTest.java →   _Bridges community 0 → community 6_
- `ReflectiveListenerInvoker` --implements--> `ListenerInvoker`  [EXTRACTED]
  mqtt-plus-core\src\main\java\io\github\mqttplus\core\invocation\ReflectiveListenerInvoker.java →   _Bridges community 2 → community 4_
- `DefaultMqttMessageRouter` --implements--> `MqttMessageRouter`  [EXTRACTED]
  mqtt-plus-core\src\main\java\io\github\mqttplus\core\router\DefaultMqttMessageRouter.java →   _Bridges community 4 → community 0_

## Communities

### Community 0 - "Community 0"
Cohesion: 0.03
Nodes (37): NoOpConnectionListener, StubFactory, StubFactory, StubFactory, StubFactory, SampleBasicApplicationTest, StubFactory, TestConfig (+29 more)

### Community 1 - "Community 1"
Cohesion: 0.05
Nodes (10): DeviceCommand, SampleBasicApplication, SpringIntegrationMqttClientAdapter, SpringIntegrationMqttClientAdapterIT, SpringIntegrationReconnectIT, SampleMultiBrokerApplication, SharedListener, PahoMqttClientAdapterIT (+2 more)

### Community 2 - "Community 2"
Cohesion: 0.04
Nodes (18): MqttPlusAutoConfigurationIT, PayloadSerializerAutoConfigurationTest, BeanPostProcessor, ReflectiveListenerInvoker, MqttListenerDefinition, MqttListenerDefinitionTest, SampleHandler, MqttListenerRegistry (+10 more)

### Community 3 - "Community 3"
Cohesion: 0.1
Nodes (9): SpringIntegrationMqttClientAdapterFactory, SpringIntegrationMqttClientAdapterTest, Builder, MqttBrokerDefinitionTest, Builder, ThreadPoolConfig, ThreadPoolConfigTest, PahoMqttClientAdapterFactoryTest (+1 more)

### Community 4 - "Community 4"
Cohesion: 0.04
Nodes (18): DefaultErrorHandlingStrategy, ErrorActionAggregator, ErrorActionAggregatorTest, ErrorHandlingStrategy, ErrorHandlingStrategy, MqttSubscriptionRefreshEvent, MqttListenerMethodArgumentResolver, MqttListenerMethodArgumentResolverTest (+10 more)

### Community 5 - "Community 5"
Cohesion: 0.07
Nodes (11): DefaultMqttClientAdapterRegistry, MqttBrokerAutoConfiguration, MqttBrokerAutoConfigurationTest, MqttClientAdapterFactoryRegistry, MqttClientAdapterFactoryRegistryTest, MqttHeaders, MqttHeadersTest, DefaultMqttMessageRouterTest (+3 more)

### Community 6 - "Community 6"
Cohesion: 0.06
Nodes (6): DefaultMqttClientAdapterRegistryTest, TestAdapter, DefaultMqttTemplateTest, RecordingAdapter, StubRegistry, DefaultMqttSubscriptionManager

### Community 7 - "Community 7"
Cohesion: 0.09
Nodes (6): AutoCloseable, PayloadSerializerIntegrationIT, MqttContext, EmbeddedMqttBroker, EmbeddedMqttBrokerInitializer, EmbeddedMqttBrokerInitializerTest

### Community 8 - "Community 8"
Cohesion: 0.1
Nodes (2): BrokerProperties, MqttPlusProperties

### Community 9 - "Community 9"
Cohesion: 0.13
Nodes (2): MqttPlusAutoConfiguration, MqttPlusAutoConfigurationTest

### Community 10 - "Community 10"
Cohesion: 0.11
Nodes (3): MqttCallbackExtended, CallbackHandler, PahoMqttClientAdapter

### Community 11 - "Community 11"
Cohesion: 0.1
Nodes (6): AlphaSerializer, CustomPayloadSerializerTest, CustomSerializerConfig, ZuluSerializer, PayloadSerializerTest, DefaultMqttTemplate

### Community 12 - "Community 12"
Cohesion: 0.14
Nodes (1): MqttBrokerDefinition

### Community 13 - "Community 13"
Cohesion: 0.17
Nodes (1): MqttClientAdapter

### Community 14 - "Community 14"
Cohesion: 0.17
Nodes (1): StubAdapter

### Community 15 - "Community 15"
Cohesion: 0.17
Nodes (1): StubAdapter

### Community 16 - "Community 16"
Cohesion: 0.17
Nodes (1): StubAdapter

### Community 17 - "Community 17"
Cohesion: 0.17
Nodes (1): StubAdapter

### Community 18 - "Community 18"
Cohesion: 0.17
Nodes (1): StubAdapter

### Community 19 - "Community 19"
Cohesion: 0.17
Nodes (1): StubAdapter

### Community 20 - "Community 20"
Cohesion: 0.18
Nodes (1): RecordingAdapter

### Community 21 - "Community 21"
Cohesion: 0.22
Nodes (3): ExponentialBackoffReconnectStrategy, FixedReconnectStrategy, ReconnectStrategy

### Community 22 - "Community 22"
Cohesion: 0.33
Nodes (2): ReconnectStrategy, ReconnectStrategyTest

### Community 23 - "Community 23"
Cohesion: 0.33
Nodes (1): MqttClientAdapterRegistry

### Community 24 - "Community 24"
Cohesion: 0.4
Nodes (1): MqttClientAdapterFactory

### Community 25 - "Community 25"
Cohesion: 0.4
Nodes (1): MqttConnectionListener

### Community 26 - "Community 26"
Cohesion: 0.4
Nodes (1): DynamicSubscriptionController

### Community 27 - "Community 27"
Cohesion: 0.5
Nodes (1): MqttTemplate

### Community 28 - "Community 28"
Cohesion: 0.5
Nodes (1): PayloadConverter

### Community 29 - "Community 29"
Cohesion: 0.5
Nodes (1): PayloadSerializer

### Community 30 - "Community 30"
Cohesion: 0.5
Nodes (1): MqttMessageInterceptor

### Community 31 - "Community 31"
Cohesion: 0.67
Nodes (1): MqttInboundMessageSink

### Community 32 - "Community 32"
Cohesion: 0.67
Nodes (1): ListenerInvoker

### Community 33 - "Community 33"
Cohesion: 0.67
Nodes (1): MqttMessageRouter

### Community 34 - "Community 34"
Cohesion: 0.67
Nodes (1): BasicStatusListener

### Community 35 - "Community 35"
Cohesion: 0.67
Nodes (1): DynamicSubscriptionListener

### Community 36 - "Community 36"
Cohesion: 0.67
Nodes (1): SampleDynamicSubscriptionApplication

## Knowledge Gaps
- **Thin community `Community 8`** (32 nodes): `.connectOptions()`, `.buildClientFactory()`, `.connectOptions()`, `MqttPlusProperties.java`, `.buildConnectOptions()`, `BrokerProperties`, `.getHost()`, `.getInboundCoreSize()`, `.getInboundMaxSize()`, `.getInboundQueueCapacity()`, `.getInboundRejectedPolicy()`, `.getMqttVersion()`, `.getPassword()`, `.getPort()`, `.getUsername()`, `.setAdapter()`, `.setCleanSession()`, `.setClientId()`, `.setConnectionTimeout()`, `.setHost()`, `.setInboundCoreSize()`, `.setInboundMaxSize()`, `.setInboundQueueCapacity()`, `.setInboundRejectedPolicy()`, `.setKeepAliveInterval()`, `.setMqttVersion()`, `.setPassword()`, `.setPort()`, `.setSslEnabled()`, `.setUsername()`, `MqttPlusProperties`, `.connectOptions()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 9`** (30 nodes): `MqttPlusAutoConfiguration`, `.addJacksonPayloadConverterIfAvailable()`, `.byteArrayPayloadSerializer()`, `.defaultErrorHandlingStrategy()`, `.errorActionAggregator()`, `.instantiateFactory()`, `.instantiateJacksonPayloadSerializer()`, `.isBuiltInSerializerBean()`, `.jacksonPayloadSerializer()`, `.listenerInvoker()`, `.mqttClientAdapterFactoryRegistry()`, `.mqttClientAdapterRegistry()`, `.mqttListenerAnnotationProcessor()`, `.mqttListenerMethodArgumentResolver()`, `.mqttListenerRegistry()`, `.mqttMessageInterceptors()`, `.mqttMessageRouter()`, `.mqttSubscriptionManager()`, `.mqttSubscriptionReconciler()`, `.mqttSubscriptionRefreshEventListener()`, `.mqttTemplate()`, `.pahoMqttClientAdapterFactory()`, `.payloadConverters()`, `.payloadSerializerChain()`, `.resolveApplicationClassLoader()`, `.springIntegrationMqttClientAdapterFactory()`, `.stringPayloadSerializer()`, `MqttPlusAutoConfigurationTest`, `.shouldCreateCoreStarterBeans()`, `.shouldRegisterBrokerAdaptersFromProperties()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 12`** (15 nodes): `MqttBrokerDefinition`, `.builder()`, `.getBrokerId()`, `.getClientId()`, `.getConnectionTimeout()`, `.getHost()`, `.getKeepAliveInterval()`, `.getPassword()`, `.getPort()`, `.getUsername()`, `.isCleanSession()`, `.isSslEnabled()`, `.MqttBrokerDefinition()`, `.requireText()`, `MqttBrokerDefinition.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 13`** (12 nodes): `MqttClientAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`, `MqttClientAdapter.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 14`** (12 nodes): `StubAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.StubAdapter()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 15`** (12 nodes): `StubAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.StubAdapter()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 16`** (12 nodes): `StubAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.StubAdapter()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 17`** (12 nodes): `StubAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.StubAdapter()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 18`** (12 nodes): `StubAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.StubAdapter()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 19`** (12 nodes): `StubAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.StubAdapter()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 20`** (11 nodes): `RecordingAdapter`, `.addConnectionListener()`, `.connect()`, `.disconnect()`, `.getBrokerDefinition()`, `.getBrokerId()`, `.publish()`, `.publishAsync()`, `.subscribe()`, `.supportsManualAck()`, `.unsubscribe()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 22`** (7 nodes): `ReconnectStrategy.java`, `ReconnectStrategyTest.java`, `ReconnectStrategy`, `.nextDelay()`, `ReconnectStrategyTest`, `.shouldCapExponentialDelayAtMaximum()`, `.shouldUseFixedDelayForAllAttempts()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 23`** (6 nodes): `MqttClientAdapterRegistry`, `.find()`, `.getAll()`, `.register()`, `.remove()`, `MqttClientAdapterRegistry.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 24`** (5 nodes): `MqttClientAdapterFactory`, `.adapterId()`, `.create()`, `.supportsMqttVersion()`, `MqttClientAdapterFactory.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 25`** (5 nodes): `MqttConnectionListener`, `.onConnected()`, `.onConnectionLost()`, `.onDisconnected()`, `MqttConnectionListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 26`** (5 nodes): `DynamicSubscriptionController`, `.DynamicSubscriptionController()`, `.subscribe()`, `.unsubscribe()`, `DynamicSubscriptionController.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 27`** (4 nodes): `MqttTemplate`, `.publish()`, `.publishAsync()`, `MqttTemplate.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 28`** (4 nodes): `PayloadConverter`, `.convert()`, `.supports()`, `PayloadConverter.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 29`** (4 nodes): `PayloadSerializer`, `.serialize()`, `.supports()`, `PayloadSerializer.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 30`** (4 nodes): `MqttMessageInterceptor`, `.afterHandle()`, `.beforeHandle()`, `MqttMessageInterceptor.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 31`** (3 nodes): `MqttInboundMessageSink`, `.onMessage()`, `MqttInboundMessageSink.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 32`** (3 nodes): `ListenerInvoker`, `.invoke()`, `ListenerInvoker.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 33`** (3 nodes): `MqttMessageRouter.java`, `MqttMessageRouter`, `.route()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 34`** (3 nodes): `BasicStatusListener`, `.onStatus()`, `BasicStatusListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 35`** (3 nodes): `DynamicSubscriptionListener`, `.onDefaultTopic()`, `DynamicSubscriptionListener.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 36`** (3 nodes): `SampleDynamicSubscriptionApplication`, `.main()`, `SampleDynamicSubscriptionApplication.java`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SpringIntegrationMqttClientAdapter` connect `Community 1` to `Community 0`, `Community 8`, `Community 3`, `Community 7`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **Why does `PahoMqttClientAdapter` connect `Community 10` to `Community 0`, `Community 1`, `Community 3`, `Community 7`, `Community 8`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **Why does `MqttPlusAutoConfiguration` connect `Community 9` to `Community 0`, `Community 5`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.1 - nodes in this community are weakly interconnected._
package io.github.mqttplus.starter.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.converter.PayloadConverter;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.router.MqttMessageRouter;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class MqttPlusAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void shouldAutoRegisterAvailableAdapterFactories() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("pahoMqttClientAdapterFactory");
            assertThat(context).hasBean("springIntegrationMqttClientAdapterFactory");
            assertThat(context.getBean("pahoMqttClientAdapterFactory", MqttClientAdapterFactory.class).getClass().getName())
                    .isEqualTo("io.github.mqttplus.paho.PahoMqttClientAdapterFactory");
            assertThat(context.getBean("springIntegrationMqttClientAdapterFactory", MqttClientAdapterFactory.class).getClass().getName())
                    .isEqualTo("io.github.mqttplus.integration.SpringIntegrationMqttClientAdapterFactory");
        });
    }

    @Test
    void shouldBindBrokerPropertiesAndRegisterAdapterWithCustomFactory() {
        contextRunner
                .withClassLoader(new FilteredClassLoader("io.github.mqttplus.paho", "io.github.mqttplus.integration"))
                .withBean(MqttClientAdapterFactory.class, StubFactory::new)
                .withPropertyValues(
                        "mqtt-plus.brokers.primary.host=127.0.0.1",
                        "mqtt-plus.brokers.primary.port=1883",
                        "mqtt-plus.brokers.primary.client-id=runner-primary",
                        "mqtt-plus.brokers.primary.keep-alive-interval=45")
                .run(context -> {
                    assertThat(context).hasSingleBean(MqttPlusProperties.class);
                    assertThat(context).hasSingleBean(MqttMessageRouter.class);
                    assertThat(context).hasSingleBean(MqttClientAdapterRegistry.class);

                    MqttPlusProperties properties = context.getBean(MqttPlusProperties.class);
                    assertThat(properties.getBrokers()).containsKey("primary");
                    assertThat(properties.getBrokers().get("primary").getClientId()).isEqualTo("runner-primary");
                    assertThat(properties.getBrokers().get("primary").getKeepAliveInterval()).isEqualTo(45);
                    assertThat(properties.getBrokers().get("primary").getAdapter()).isNull();
                    assertThat(properties.getBrokers().get("primary").isCleanSession()).isTrue();

                    MqttClientAdapterRegistry registry = context.getBean(MqttClientAdapterRegistry.class);
                    assertThat(registry.find("primary")).isPresent();
                    assertThat(((StubAdapter) registry.find("primary").orElseThrow()).connected).isTrue();
                });
    }

    @Test
    void shouldPreferSpringIntegrationWhenMultipleFactoriesAreAvailable() {
        contextRunner.run(context -> {
            MqttClientAdapterFactoryRegistry registry = context.getBean(MqttClientAdapterFactoryRegistry.class);
            assertThat(registry.resolveFactory(null, "3.1.1").adapterId()).isEqualTo("spring-integration");
        });
    }

    @Test
    void shouldStartWithoutJacksonOnClasspath() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("com.fasterxml.jackson.databind"))
                .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    List<PayloadConverter> converters = context.getBean("mqttPlusPayloadConverters", List.class);
                    assertThat(converters).hasSize(2);
                });
    }

    @Test
    void shouldFailFastWhenPahoFactoryIsMissingFromClasspath() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("io.github.mqttplus.paho", "io.github.mqttplus.integration"))
                .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withPropertyValues(
                        "mqtt-plus.brokers.primary.host=127.0.0.1",
                        "mqtt-plus.brokers.primary.client-id=runner-primary",
                        "mqtt-plus.brokers.primary.adapter=paho")
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("No MQTT adapter factory registered for adapter: paho");
                });
    }

    @Test
    void shouldFailFastWhenAdapterFactoryIsMissing() {
        new ApplicationContextRunner()
                .withClassLoader(new FilteredClassLoader("io.github.mqttplus.paho", "io.github.mqttplus.integration"))
                .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class))
                .withBean(ObjectMapper.class, ObjectMapper::new)
                .withPropertyValues(
                        "mqtt-plus.brokers.primary.host=127.0.0.1",
                        "mqtt-plus.brokers.primary.client-id=runner-primary",
                        "mqtt-plus.brokers.primary.mqtt-version=5.0")
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNotNull();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("No MQTT adapter factory registered for version: 5.0");
                });
    }

    private static final class StubFactory implements MqttClientAdapterFactory {
        @Override
        public String adapterId() {
            return "paho";
        }

        @Override
        public boolean supportsMqttVersion(String mqttVersion) {
            return "3.1.1".equals(mqttVersion);
        }

        @Override
        public MqttClientAdapter create(MqttBrokerDefinition brokerDefinition, MqttInboundMessageSink inboundMessageSink) {
            return new StubAdapter(brokerDefinition);
        }
    }

    private static final class StubAdapter implements MqttClientAdapter {
        private final MqttBrokerDefinition brokerDefinition;
        private boolean connected;

        private StubAdapter(MqttBrokerDefinition brokerDefinition) {
            this.brokerDefinition = brokerDefinition;
        }

        @Override
        public String getBrokerId() {
            return brokerDefinition.getBrokerId();
        }

        @Override
        public MqttBrokerDefinition getBrokerDefinition() {
            return brokerDefinition;
        }

        @Override
        public void connect() {
            connected = true;
        }

        @Override
        public void disconnect() {
            connected = false;
        }

        @Override
        public void subscribe(String topic, int qos) {
        }

        @Override
        public void unsubscribe(String topic) {
        }

        @Override
        public void publish(String topic, byte[] payload) {
            publish(topic, payload, 0, false);
        }

        @Override
        public void publish(String topic, byte[] payload, int qos, boolean retained) {
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload) {
            return publishAsync(topic, payload, 0, false);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, Executor executor) {
            return publishAsync(topic, payload, 0, false, executor);
        }

        @Override
        public CompletableFuture<Void> publishAsync(String topic, byte[] payload, int qos, boolean retained, Executor executor) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean supportsManualAck() {
            return false;
        }

        @Override
        public void addConnectionListener(MqttConnectionListener listener) {
        }
    }
}
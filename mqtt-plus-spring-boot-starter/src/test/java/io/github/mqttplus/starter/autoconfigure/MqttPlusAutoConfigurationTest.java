package io.github.mqttplus.starter.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqttplus.core.MqttTemplate;
import io.github.mqttplus.core.router.MqttMessageRouter;
import io.github.mqttplus.paho.PahoMqttClientAdapterFactory;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MqttPlusAutoConfigurationTest {

    @Test
    void shouldCreateCoreStarterBeans() {
        MqttPlusAutoConfiguration configuration = new MqttPlusAutoConfiguration();
        ObjectProvider<ObjectMapper> objectMapperProvider = new SimpleObjectProvider<>(new ObjectMapper());

        assertNotNull(configuration.mqttPlusProperties());
        assertNotNull(configuration.mqttClientAdapterRegistry());
        assertNotNull(configuration.mqttListenerRegistry());
        assertNotNull(configuration.mqttSubscriptionManager());
        assertNotNull(configuration.errorActionAggregator());
        assertNotNull(configuration.defaultErrorHandlingStrategy());
        assertNotNull(configuration.listenerInvoker());
        assertNotNull(configuration.mqttMessageInterceptors());
        assertNotNull(configuration.payloadConverters(objectMapperProvider));

        MqttMessageRouter router = configuration.mqttMessageRouter(
                configuration.mqttListenerRegistry(),
                configuration.listenerInvoker(),
                configuration.defaultErrorHandlingStrategy(),
                configuration.errorActionAggregator(),
                configuration.payloadConverters(objectMapperProvider),
                configuration.mqttMessageInterceptors());
        MqttTemplate template = configuration.mqttTemplate(configuration.mqttClientAdapterRegistry());

        assertNotNull(router);
        assertNotNull(template);
        assertNotNull(configuration.mqttListenerAnnotationProcessor(configuration.mqttListenerRegistry()));
        assertNotNull(configuration.mqttListenerMethodArgumentResolver());
        assertNotNull(configuration.mqttSubscriptionRefreshEventListener(configuration.mqttSubscriptionManager()));
        assertNotNull(configuration.mqttClientAdapterFactoryRegistry(List.of(new PahoMqttClientAdapterFactory())));
    }

    @Test
    void shouldRegisterBrokerAdaptersFromProperties() {
        MqttPlusProperties properties = new MqttPlusProperties();
        MqttPlusProperties.BrokerProperties broker = new MqttPlusProperties.BrokerProperties();
        broker.setHost("127.0.0.1");
        broker.setClientId("starter-test");
        properties.getBrokers().put("primary", broker);

        MqttPlusAutoConfiguration configuration = new MqttPlusAutoConfiguration();
        MqttBrokerAutoConfiguration brokerAutoConfiguration = new MqttBrokerAutoConfiguration();
        var registry = configuration.mqttClientAdapterRegistry();

        brokerAutoConfiguration.registerAdapters(
                properties,
                configuration.mqttClientAdapterFactoryRegistry(List.of(new PahoMqttClientAdapterFactory())),
                registry,
                (brokerId, topic, payload, headers) -> {
                },
                configuration.mqttSubscriptionReconciler(
                        registry,
                        configuration.mqttListenerRegistry(),
                        configuration.mqttSubscriptionManager()));

        assertNotNull(registry.find("primary").orElse(null));
    }

    private static final class SimpleObjectProvider<T> implements ObjectProvider<T> {
        private final T value;

        private SimpleObjectProvider(T value) {
            this.value = value;
        }

        @Override
        public T getObject(Object... args) {
            return value;
        }

        @Override
        public T getIfAvailable() {
            return value;
        }

        @Override
        public T getIfUnique() {
            return value;
        }

        @Override
        public T getObject() {
            return value;
        }
    }
}
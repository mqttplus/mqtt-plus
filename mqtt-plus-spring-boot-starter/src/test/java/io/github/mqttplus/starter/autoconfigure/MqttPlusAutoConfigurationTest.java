package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.MqttTemplate;
import io.github.mqttplus.core.adapter.MqttClientAdapter;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.adapter.MqttInboundMessageSink;
import io.github.mqttplus.core.converter.PayloadSerializer;
import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.router.MqttMessageRouter;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MqttPlusAutoConfigurationTest {

    @Test
    void shouldCreateCoreStarterBeans() {
        MqttPlusAutoConfiguration configuration = new MqttPlusAutoConfiguration();
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        assertNotNull(configuration.mqttClientAdapterRegistry());
        assertNotNull(configuration.mqttListenerRegistry());
        assertNotNull(configuration.mqttSubscriptionManager());
        assertNotNull(configuration.errorActionAggregator());
        assertNotNull(configuration.defaultErrorHandlingStrategy());
        assertNotNull(configuration.listenerInvoker(configuration.mqttListenerMethodArgumentResolver()));
        assertNotNull(configuration.mqttMessageInterceptors());
        assertNotNull(configuration.payloadConverters(beanFactory));

        MqttMessageRouter router = configuration.mqttMessageRouter(
                configuration.mqttListenerRegistry(),
                configuration.listenerInvoker(configuration.mqttListenerMethodArgumentResolver()),
                configuration.defaultErrorHandlingStrategy(),
                configuration.errorActionAggregator(),
                configuration.payloadConverters(beanFactory),
                configuration.mqttMessageInterceptors());
        List<PayloadSerializer> payloadSerializers = configuration.payloadSerializerChain(
                beanFactory,
                configuration.byteArrayPayloadSerializer(),
                configuration.stringPayloadSerializer(),
                new StaticListableBeanFactory().getBeanProvider(PayloadSerializer.class));
        MqttTemplate template = configuration.mqttTemplate(configuration.mqttClientAdapterRegistry(), payloadSerializers);

        assertNotNull(router);
        assertNotNull(template);
        assertNotNull(configuration.mqttListenerAnnotationProcessor(configuration.mqttListenerRegistry()));
        assertNotNull(configuration.mqttListenerMethodArgumentResolver());
        assertNotNull(configuration.mqttSubscriptionRefreshEventListener(configuration.mqttSubscriptionManager(), configuration.mqttClientAdapterRegistry()));
        assertNotNull(configuration.mqttClientAdapterFactoryRegistry(List.of(new StubFactory())));
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
        StubFactory stubFactory = new StubFactory();

        brokerAutoConfiguration.registerAdapters(
                properties,
                configuration.mqttClientAdapterFactoryRegistry(List.of(stubFactory)),
                registry,
                (brokerId, topic, payload, headers) -> {
                },
                List.of(configuration.mqttSubscriptionReconciler(
                        registry,
                        configuration.mqttListenerRegistry(),
                        configuration.mqttSubscriptionManager())));

        assertNotNull(registry.find("primary").orElse(null));
        assertTrue(stubFactory.lastAdapter.connected);
    }

    private static final class StubFactory implements MqttClientAdapterFactory {
        private StubAdapter lastAdapter;

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
            this.lastAdapter = new StubAdapter(brokerDefinition);
            return lastAdapter;
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
        public boolean supportsManualAck() {
            return false;
        }

        @Override
        public void addConnectionListener(MqttConnectionListener listener) {
        }
    }
}
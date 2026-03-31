package io.github.mqttplus.starter.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mqttplus.core.DefaultMqttTemplate;
import io.github.mqttplus.core.MqttTemplate;
import io.github.mqttplus.core.adapter.DefaultMqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.converter.PayloadConverter;
import io.github.mqttplus.core.error.DefaultErrorHandlingStrategy;
import io.github.mqttplus.core.error.ErrorActionAggregator;
import io.github.mqttplus.core.interceptor.MqttMessageInterceptor;
import io.github.mqttplus.core.invocation.ListenerInvoker;
import io.github.mqttplus.core.invocation.ReflectiveListenerInvoker;
import io.github.mqttplus.core.router.DefaultMqttMessageRouter;
import io.github.mqttplus.core.router.MqttListenerRegistry;
import io.github.mqttplus.core.router.MqttMessageRouter;
import io.github.mqttplus.core.subscription.DefaultMqttSubscriptionManager;
import io.github.mqttplus.core.subscription.MqttSubscriptionManager;
import io.github.mqttplus.core.subscription.MqttSubscriptionReconciler;
import io.github.mqttplus.spring.MqttListenerAnnotationProcessor;
import io.github.mqttplus.spring.event.MqttSubscriptionRefreshEventListener;
import io.github.mqttplus.spring.invocation.MqttListenerMethodArgumentResolver;
import io.github.mqttplus.starter.converter.ByteArrayPayloadConverter;
import io.github.mqttplus.starter.converter.JacksonPayloadConverter;
import io.github.mqttplus.starter.converter.StringPayloadConverter;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
public class MqttPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MqttPlusProperties mqttPlusProperties() {
        return new MqttPlusProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttClientAdapterRegistry mqttClientAdapterRegistry() {
        return new DefaultMqttClientAdapterRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttListenerRegistry mqttListenerRegistry() {
        return new MqttListenerRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttSubscriptionManager mqttSubscriptionManager() {
        return new DefaultMqttSubscriptionManager();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorActionAggregator errorActionAggregator() {
        return new ErrorActionAggregator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultErrorHandlingStrategy defaultErrorHandlingStrategy() {
        return new DefaultErrorHandlingStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public ListenerInvoker listenerInvoker() {
        return new ReflectiveListenerInvoker();
    }

    @Bean(name = "mqttPlusPayloadConverters")
    @ConditionalOnMissingBean(name = "mqttPlusPayloadConverters")
    public List<PayloadConverter> payloadConverters(ObjectProvider<ObjectMapper> objectMapperProvider) {
        List<PayloadConverter> converters = new ArrayList<>();
        converters.add(new ByteArrayPayloadConverter());
        converters.add(new StringPayloadConverter());
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (objectMapper != null) {
            converters.add(new JacksonPayloadConverter(objectMapper));
        }
        return converters;
    }

    @Bean(name = "mqttMessageInterceptors")
    @ConditionalOnMissingBean(name = "mqttMessageInterceptors")
    public List<MqttMessageInterceptor> mqttMessageInterceptors() {
        return List.of();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttMessageRouter mqttMessageRouter(MqttListenerRegistry listenerRegistry,
                                               ListenerInvoker listenerInvoker,
                                               DefaultErrorHandlingStrategy errorHandlingStrategy,
                                               ErrorActionAggregator errorActionAggregator,
                                               List<PayloadConverter> payloadConverters,
                                               List<MqttMessageInterceptor> interceptors) {
        return new DefaultMqttMessageRouter(listenerRegistry, payloadConverters, interceptors, listenerInvoker, errorHandlingStrategy, errorActionAggregator);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttTemplate mqttTemplate(MqttClientAdapterRegistry registry) {
        return new DefaultMqttTemplate(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttSubscriptionReconciler mqttSubscriptionReconciler(MqttClientAdapterRegistry registry,
                                                                 MqttListenerRegistry listenerRegistry,
                                                                 MqttSubscriptionManager subscriptionManager) {
        return new MqttSubscriptionReconciler(registry, listenerRegistry, subscriptionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttListenerAnnotationProcessor mqttListenerAnnotationProcessor(MqttListenerRegistry listenerRegistry) {
        return new MqttListenerAnnotationProcessor(listenerRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttListenerMethodArgumentResolver mqttListenerMethodArgumentResolver() {
        return new MqttListenerMethodArgumentResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttSubscriptionRefreshEventListener mqttSubscriptionRefreshEventListener(MqttSubscriptionManager subscriptionManager) {
        return new MqttSubscriptionRefreshEventListener(subscriptionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttClientAdapterFactoryRegistry mqttClientAdapterFactoryRegistry(List<MqttClientAdapterFactory> factories) {
        return new MqttClientAdapterFactoryRegistry(factories);
    }

    @Bean
    public InitializingBean mqttBrokerAdapterRegistrar(MqttPlusProperties properties,
                                                       MqttClientAdapterFactoryRegistry factoryRegistry,
                                                       MqttClientAdapterRegistry adapterRegistry,
                                                       MqttMessageRouter mqttMessageRouter,
                                                       MqttSubscriptionReconciler subscriptionReconciler) {
        return () -> new MqttBrokerAutoConfiguration().registerAdapters(
                properties,
                factoryRegistry,
                adapterRegistry,
                mqttMessageRouter::route,
                subscriptionReconciler);
    }
}
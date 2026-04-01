package io.github.mqttplus.starter.autoconfigure;

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
import io.github.mqttplus.starter.converter.StringPayloadConverter;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(MqttPlusProperties.class)
public class MqttPlusAutoConfiguration {

    private static final String OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";
    private static final String JACKSON_CONVERTER_CLASS_NAME = "io.github.mqttplus.starter.converter.JacksonPayloadConverter";
    private static final String PAHO_FACTORY_CLASS_NAME = "io.github.mqttplus.paho.PahoMqttClientAdapterFactory";

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
    public List<PayloadConverter> payloadConverters(ListableBeanFactory beanFactory) {
        List<PayloadConverter> converters = new ArrayList<>();
        converters.add(new ByteArrayPayloadConverter());
        converters.add(new StringPayloadConverter());
        addJacksonPayloadConverterIfAvailable(converters, beanFactory);
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
    public MqttSubscriptionRefreshEventListener mqttSubscriptionRefreshEventListener(MqttSubscriptionManager subscriptionManager,
                                                                                     MqttClientAdapterRegistry adapterRegistry) {
        return new MqttSubscriptionRefreshEventListener(subscriptionManager, adapterRegistry);
    }

    @Bean
    @ConditionalOnClass(name = PAHO_FACTORY_CLASS_NAME)
    @ConditionalOnMissingBean(name = "pahoMqttClientAdapterFactory")
    public MqttClientAdapterFactory pahoMqttClientAdapterFactory() {
        return instantiatePahoFactory();
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

    private void addJacksonPayloadConverterIfAvailable(List<PayloadConverter> converters, ListableBeanFactory beanFactory) {
        try {
            ClassLoader classLoader = resolveApplicationClassLoader();
            Class<?> objectMapperClass = Class.forName(OBJECT_MAPPER_CLASS_NAME, false, classLoader);
            Object objectMapper = beanFactory.getBeanProvider(objectMapperClass).getIfAvailable();
            if (objectMapper == null) {
                return;
            }
            Class<?> converterClass = Class.forName(JACKSON_CONVERTER_CLASS_NAME, false, classLoader);
            Constructor<?> constructor = converterClass.getConstructor(objectMapperClass);
            PayloadConverter converter = (PayloadConverter) constructor.newInstance(objectMapper);
            converters.add(converter);
        } catch (ClassNotFoundException ex) {
            // Jackson is optional for starter users; skip JSON conversion when absent.
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to initialize optional Jackson payload converter", ex);
        }
    }

    private MqttClientAdapterFactory instantiatePahoFactory() {
        try {
            Class<?> factoryClass = Class.forName(PAHO_FACTORY_CLASS_NAME, false, resolveApplicationClassLoader());
            return (MqttClientAdapterFactory) factoryClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to initialize optional Paho MQTT adapter factory", ex);
        }
    }

    private ClassLoader resolveApplicationClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }
        return MqttPlusAutoConfiguration.class.getClassLoader();
    }
}
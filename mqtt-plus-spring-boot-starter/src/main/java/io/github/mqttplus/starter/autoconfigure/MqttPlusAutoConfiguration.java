package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.DefaultMqttTemplate;
import io.github.mqttplus.core.MqttTemplate;
import io.github.mqttplus.core.adapter.DefaultMqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttClientAdapterFactory;
import io.github.mqttplus.core.adapter.MqttClientAdapterRegistry;
import io.github.mqttplus.core.adapter.MqttConnectionListener;
import io.github.mqttplus.core.converter.PayloadConverter;
import io.github.mqttplus.core.converter.PayloadSerializer;
import io.github.mqttplus.core.error.DefaultErrorHandlingStrategy;
import io.github.mqttplus.core.error.ErrorActionAggregator;
import io.github.mqttplus.core.interceptor.MqttMessageInterceptor;
import io.github.mqttplus.core.invocation.ListenerInvoker;
import io.github.mqttplus.core.router.DefaultMqttMessageRouter;
import io.github.mqttplus.core.router.MqttListenerRegistry;
import io.github.mqttplus.core.router.MqttMessageRouter;
import io.github.mqttplus.core.subscription.DefaultMqttSubscriptionManager;
import io.github.mqttplus.core.subscription.MqttSubscriptionManager;
import io.github.mqttplus.core.subscription.MqttSubscriptionReconciler;
import io.github.mqttplus.spring.MqttListenerAnnotationProcessor;
import io.github.mqttplus.spring.event.MqttSubscriptionRefreshEventListener;
import io.github.mqttplus.spring.invocation.MqttListenerMethodArgumentResolver;
import io.github.mqttplus.spring.invocation.SpringMqttListenerInvoker;
import io.github.mqttplus.starter.converter.ByteArrayPayloadConverter;
import io.github.mqttplus.starter.converter.ByteArrayPayloadSerializer;
import io.github.mqttplus.starter.converter.StringPayloadConverter;
import io.github.mqttplus.starter.converter.StringPayloadSerializer;
import io.github.mqttplus.starter.properties.MqttPlusProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(MqttPlusProperties.class)
public class MqttPlusAutoConfiguration {

    private static final String OBJECT_MAPPER_CLASS_NAME = "com.fasterxml.jackson.databind.ObjectMapper";
    private static final String JACKSON_CONVERTER_CLASS_NAME = "io.github.mqttplus.starter.converter.JacksonPayloadConverter";
    private static final String JACKSON_SERIALIZER_CLASS_NAME = "io.github.mqttplus.starter.converter.JacksonPayloadSerializer";
    private static final String PAHO_FACTORY_CLASS_NAME = "io.github.mqttplus.paho.PahoMqttClientAdapterFactory";
    private static final String SPRING_INTEGRATION_FACTORY_CLASS_NAME = "io.github.mqttplus.integration.SpringIntegrationMqttClientAdapterFactory";
    private static final String BYTE_ARRAY_SERIALIZER_BEAN_NAME = "byteArrayPayloadSerializer";
    private static final String STRING_SERIALIZER_BEAN_NAME = "stringPayloadSerializer";
    private static final String JACKSON_SERIALIZER_BEAN_NAME = "jacksonPayloadSerializer";

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
    public ListenerInvoker listenerInvoker(MqttListenerMethodArgumentResolver argumentResolver) {
        return new SpringMqttListenerInvoker(argumentResolver);
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

    @Bean(name = BYTE_ARRAY_SERIALIZER_BEAN_NAME)
    @ConditionalOnMissingBean(name = BYTE_ARRAY_SERIALIZER_BEAN_NAME)
    public PayloadSerializer byteArrayPayloadSerializer() {
        return new ByteArrayPayloadSerializer();
    }

    @Bean(name = STRING_SERIALIZER_BEAN_NAME)
    @ConditionalOnMissingBean(name = STRING_SERIALIZER_BEAN_NAME)
    public PayloadSerializer stringPayloadSerializer() {
        return new StringPayloadSerializer();
    }

    @Bean(name = JACKSON_SERIALIZER_BEAN_NAME)
    @ConditionalOnMissingBean(name = JACKSON_SERIALIZER_BEAN_NAME)
    @ConditionalOnClass(name = OBJECT_MAPPER_CLASS_NAME)
    public PayloadSerializer jacksonPayloadSerializer(ListableBeanFactory beanFactory) {
        return instantiateJacksonPayloadSerializer(beanFactory);
    }

    @Bean(name = "mqttPlusPayloadSerializerChain")
    @ConditionalOnMissingBean(name = "mqttPlusPayloadSerializerChain")
    public List<PayloadSerializer> payloadSerializerChain(
            ListableBeanFactory beanFactory,
            @Qualifier(BYTE_ARRAY_SERIALIZER_BEAN_NAME) PayloadSerializer byteArraySerializer,
            @Qualifier(STRING_SERIALIZER_BEAN_NAME) PayloadSerializer stringSerializer,
            @Qualifier(JACKSON_SERIALIZER_BEAN_NAME) ObjectProvider<PayloadSerializer> jacksonSerializerProvider) {
        List<PayloadSerializer> serializers = new ArrayList<>();
        Map<String, PayloadSerializer> allSerializers = beanFactory.getBeansOfType(PayloadSerializer.class);
        allSerializers.entrySet().stream()
                .filter(entry -> !isBuiltInSerializerBean(entry.getKey()))
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(Map.Entry::getValue)
                .forEach(serializers::add);
        serializers.add(byteArraySerializer);
        serializers.add(stringSerializer);
        PayloadSerializer jacksonSerializer = jacksonSerializerProvider.getIfAvailable();
        if (jacksonSerializer != null) {
            serializers.add(jacksonSerializer);
        }
        return List.copyOf(serializers);
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
    public MqttTemplate mqttTemplate(MqttClientAdapterRegistry registry,
                                     @Qualifier("mqttPlusPayloadSerializerChain") List<PayloadSerializer> payloadSerializers) {
        return new DefaultMqttTemplate(registry, payloadSerializers);
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
        return instantiateFactory(PAHO_FACTORY_CLASS_NAME);
    }

    @Bean
    @ConditionalOnClass(name = SPRING_INTEGRATION_FACTORY_CLASS_NAME)
    @ConditionalOnMissingBean(name = "springIntegrationMqttClientAdapterFactory")
    public MqttClientAdapterFactory springIntegrationMqttClientAdapterFactory() {
        return instantiateFactory(SPRING_INTEGRATION_FACTORY_CLASS_NAME);
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
                                                       List<MqttConnectionListener> connectionListeners) {
        return () -> new MqttBrokerAutoConfiguration().registerAdapters(
                properties,
                factoryRegistry,
                adapterRegistry,
                mqttMessageRouter::route,
                connectionListeners);
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
        }
        catch (ClassNotFoundException ex) {
            // Jackson is optional for starter users; skip JSON conversion when absent.
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to initialize optional Jackson payload converter", ex);
        }
    }

    private PayloadSerializer instantiateJacksonPayloadSerializer(ListableBeanFactory beanFactory) {
        try {
            ClassLoader classLoader = resolveApplicationClassLoader();
            Class<?> objectMapperClass = Class.forName(OBJECT_MAPPER_CLASS_NAME, false, classLoader);
            Object objectMapper = beanFactory.getBeanProvider(objectMapperClass).getIfAvailable();
            if (objectMapper == null) {
                objectMapper = objectMapperClass.getDeclaredConstructor().newInstance();
            }
            Class<?> serializerClass = Class.forName(JACKSON_SERIALIZER_CLASS_NAME, false, classLoader);
            Constructor<?> constructor = serializerClass.getConstructor(objectMapperClass);
            return (PayloadSerializer) constructor.newInstance(objectMapper);
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Jackson payload serializer class is not available", ex);
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to initialize optional Jackson payload serializer", ex);
        }
    }

    private boolean isBuiltInSerializerBean(String beanName) {
        return BYTE_ARRAY_SERIALIZER_BEAN_NAME.equals(beanName)
                || STRING_SERIALIZER_BEAN_NAME.equals(beanName)
                || JACKSON_SERIALIZER_BEAN_NAME.equals(beanName);
    }

    private MqttClientAdapterFactory instantiateFactory(String factoryClassName) {
        try {
            Class<?> factoryClass = Class.forName(factoryClassName, false, resolveApplicationClassLoader());
            return (MqttClientAdapterFactory) factoryClass.getDeclaredConstructor().newInstance();
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to initialize optional MQTT adapter factory: " + factoryClassName, ex);
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

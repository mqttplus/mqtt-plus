package io.github.mqttplus.test;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class EmbeddedMqttBrokerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        EmbeddedMqttBroker broker = EmbeddedMqttBroker.startDefault();
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("mqtt-plus.brokers.primary.host", broker.getHost());
        properties.put("mqtt-plus.brokers.primary.port", broker.getPort());
        properties.put("mqtt-plus.brokers.primary.client-id", "mqtt-plus-test-" + UUID.randomUUID());
        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("mqttPlusEmbeddedBroker", properties));
        applicationContext.getBeanFactory().registerSingleton("embeddedMqttBroker", broker);
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                broker.stop();
            }
        });
    }
}
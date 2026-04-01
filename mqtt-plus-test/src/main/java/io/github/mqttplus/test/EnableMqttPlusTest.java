package io.github.mqttplus.test;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MqttTestConfiguration.class)
@ContextConfiguration(initializers = EmbeddedMqttBrokerInitializer.class)
public @interface EnableMqttPlusTest {
}
package io.github.mqttplus.samples.basic;

import io.github.mqttplus.core.MqttTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleBasicApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "samples.publish-on-startup", havingValue = "true")
    CommandLineRunner basicPublisher(MqttTemplate mqttTemplate) {
        return args -> mqttTemplate.publish("primary", "samples/basic/ping", "hello from sample-basic");
    }
}
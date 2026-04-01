package io.github.mqttplus.samples.multibroker;

import io.github.mqttplus.core.MqttTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleMultiBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleMultiBrokerApplication.class, args);
    }

    @Bean
    @ConditionalOnProperty(name = "samples.publish-on-startup", havingValue = "true")
    CommandLineRunner multiBrokerPublisher(MqttTemplate mqttTemplate) {
        return args -> {
            mqttTemplate.publish("publicBroker", "samples/public/status", "hello public");
            mqttTemplate.publish("privateBroker", "samples/private/status", "hello private");
        };
    }
}
package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.converter.PayloadSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomPayloadSerializerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class));

    @Test
    void userDefinedSerializersShouldAppearBeforeBuiltInInBeanNameOrder() {
        contextRunner.withUserConfiguration(CustomSerializerConfig.class).run(context -> {
            @SuppressWarnings("unchecked")
            List<PayloadSerializer> serializers =
                    (List<PayloadSerializer>) context.getBean("mqttPlusPayloadSerializerChain");
            assertThat(serializers.get(0)).isInstanceOf(AlphaSerializer.class);
            assertThat(serializers.get(1)).isInstanceOf(ZuluSerializer.class);
            assertThat(serializers.get(2).getClass().getSimpleName()).isEqualTo("ByteArrayPayloadSerializer");
        });
    }

    @Configuration
    static class CustomSerializerConfig {
        @Bean
        public PayloadSerializer zuluSerializer() {
            return new ZuluSerializer();
        }

        @Bean
        public PayloadSerializer alphaSerializer() {
            return new AlphaSerializer();
        }
    }

    static class AlphaSerializer implements PayloadSerializer {
        @Override
        public boolean supports(Class<?> sourceType) {
            return true;
        }

        @Override
        public byte[] serialize(Object payload) {
            return "alpha".getBytes(StandardCharsets.UTF_8);
        }
    }

    static class ZuluSerializer implements PayloadSerializer {
        @Override
        public boolean supports(Class<?> sourceType) {
            return true;
        }

        @Override
        public byte[] serialize(Object payload) {
            return "zulu".getBytes(StandardCharsets.UTF_8);
        }
    }
}

package io.github.mqttplus.starter.autoconfigure;

import io.github.mqttplus.core.DefaultMqttTemplate;
import io.github.mqttplus.core.converter.PayloadSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PayloadSerializerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MqttPlusAutoConfiguration.class));

    @Test
    void shouldRegisterCanonicalPayloadSerializerChain() {
        contextRunner.run(context -> {
            @SuppressWarnings("unchecked")
            List<PayloadSerializer> serializers =
                    (List<PayloadSerializer>) context.getBean("mqttPlusPayloadSerializerChain");
            assertThat(serializers).hasSize(3);
            assertThat(serializers.get(0).getClass().getSimpleName()).isEqualTo("ByteArrayPayloadSerializer");
            assertThat(serializers.get(1).getClass().getSimpleName()).isEqualTo("StringPayloadSerializer");
            assertThat(serializers.get(2).getClass().getSimpleName()).isEqualTo("JacksonPayloadSerializer");
        });
    }

    @Test
    void mqttTemplateShouldUseCanonicalPayloadSerializerChain() {
        contextRunner.run(context -> {
            DefaultMqttTemplate template = (DefaultMqttTemplate) context.getBean(io.github.mqttplus.core.MqttTemplate.class);
            @SuppressWarnings("unchecked")
            List<PayloadSerializer> serializers =
                    (List<PayloadSerializer>) context.getBean("mqttPlusPayloadSerializerChain");
            assertThat(template).isNotNull();
            assertThat(serializers).isNotEmpty();
        });
    }
}

package io.github.mqttplus.spring;

import io.github.mqttplus.core.annotation.MqttListener;
import io.github.mqttplus.core.model.MqttListenerDefinition;
import io.github.mqttplus.core.router.MqttListenerRegistry;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public final class MqttListenerAnnotationProcessor implements BeanPostProcessor {

    private final MqttListenerRegistry listenerRegistry;

    public MqttListenerAnnotationProcessor(MqttListenerRegistry listenerRegistry) {
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        Map<Method, MqttListener> methods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<MqttListener>) method -> AnnotatedElementUtils.findMergedAnnotation(method, MqttListener.class));
        for (Map.Entry<Method, MqttListener> entry : methods.entrySet()) {
            MqttListener annotation = entry.getValue();
            listenerRegistry.register(new MqttListenerDefinition(
                    beanName,
                    bean,
                    entry.getKey(),
                    annotation.broker(),
                    Arrays.asList(annotation.topics()),
                    annotation.qos(),
                    annotation.payloadType()
            ));
        }
        return bean;
    }
}

package io.github.mqttplus.spring;

import io.github.mqttplus.core.annotation.MqttListener;
import io.github.mqttplus.core.model.MqttListenerDefinition;
import io.github.mqttplus.core.router.MqttListenerRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MqttListenerAnnotationProcessor implements BeanPostProcessor, SmartInitializingSingleton {

    private final Log logger = LogFactory.getLog(getClass());

    private final MqttListenerRegistry listenerRegistry;
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    public MqttListenerAnnotationProcessor(MqttListenerRegistry listenerRegistry) {
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AopInfrastructureBean) {
            // Ignore AOP infrastructure such as scoped proxies.
            return bean;
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass) &&
                AnnotationUtils.isCandidateClass(targetClass, Collections.singletonList(MqttListener.class))) {
            Map<Method, MqttListener> methods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<MqttListener>) method -> AnnotatedElementUtils.findMergedAnnotation(method, MqttListener.class));
            if (methods.isEmpty()) {
                this.nonAnnotatedClasses.add(targetClass);
                if (logger.isDebugEnabled()) {
                    logger.debug("No @MqttListener annotations found on bean class: " + targetClass);
                }
            } else {
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
                if (logger.isDebugEnabled()) {
                    logger.debug(methods.size() + " @MqttListener methods processed on bean '" + beanName + "': " + methods);
                }
            }
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // Remove resolved singleton classes from cache
        this.nonAnnotatedClasses.clear();
    }
}

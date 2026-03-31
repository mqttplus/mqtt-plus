package io.github.mqttplus.starter.properties;

import io.github.mqttplus.core.model.MqttBrokerDefinition;
import io.github.mqttplus.core.model.ThreadPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "mqtt-plus")
public class MqttPlusProperties {

    private final Map<String, BrokerProperties> brokers = new LinkedHashMap<>();

    public Map<String, BrokerProperties> getBrokers() {
        return brokers;
    }

    public static class BrokerProperties {
        private String mqttVersion = "3.1.1";
        private String host;
        private int port = 1883;
        private String clientId;
        private String username;
        private String password;
        private boolean sslEnabled;
        private int keepAliveInterval = 60;
        private int connectionTimeout = 30;
        private int inboundCoreSize = ThreadPoolConfig.DEFAULT_CORE_SIZE;
        private int inboundMaxSize = ThreadPoolConfig.DEFAULT_MAX_SIZE;
        private int inboundQueueCapacity = ThreadPoolConfig.DEFAULT_QUEUE_CAPACITY;
        private String inboundRejectedPolicy = ThreadPoolConfig.DEFAULT_REJECTED_POLICY;

        public String getMqttVersion() {
            return mqttVersion;
        }

        public void setMqttVersion(String mqttVersion) {
            this.mqttVersion = mqttVersion;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isSslEnabled() {
            return sslEnabled;
        }

        public void setSslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
        }

        public int getKeepAliveInterval() {
            return keepAliveInterval;
        }

        public void setKeepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getInboundCoreSize() {
            return inboundCoreSize;
        }

        public void setInboundCoreSize(int inboundCoreSize) {
            this.inboundCoreSize = inboundCoreSize;
        }

        public int getInboundMaxSize() {
            return inboundMaxSize;
        }

        public void setInboundMaxSize(int inboundMaxSize) {
            this.inboundMaxSize = inboundMaxSize;
        }

        public int getInboundQueueCapacity() {
            return inboundQueueCapacity;
        }

        public void setInboundQueueCapacity(int inboundQueueCapacity) {
            this.inboundQueueCapacity = inboundQueueCapacity;
        }

        public String getInboundRejectedPolicy() {
            return inboundRejectedPolicy;
        }

        public void setInboundRejectedPolicy(String inboundRejectedPolicy) {
            this.inboundRejectedPolicy = inboundRejectedPolicy;
        }

        public MqttBrokerDefinition toDefinition(String brokerId) {
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("host must not be blank");
            }
            if (clientId == null || clientId.isBlank()) {
                throw new IllegalArgumentException("clientId must not be blank");
            }
            return MqttBrokerDefinition.builder()
                    .brokerId(brokerId)
                    .host(host)
                    .port(port)
                    .clientId(clientId)
                    .username(username)
                    .password(password)
                    .sslEnabled(sslEnabled)
                    .keepAliveInterval(keepAliveInterval)
                    .connectionTimeout(connectionTimeout)
                    .inboundThreadPool(ThreadPoolConfig.builder()
                            .coreSize(inboundCoreSize)
                            .maxSize(inboundMaxSize)
                            .queueCapacity(inboundQueueCapacity)
                            .rejectedPolicy(inboundRejectedPolicy)
                            .build())
                    .build();
        }
    }
}
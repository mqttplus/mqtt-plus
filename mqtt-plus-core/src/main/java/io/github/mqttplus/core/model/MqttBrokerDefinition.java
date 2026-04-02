package io.github.mqttplus.core.model;

import java.util.Objects;

public final class MqttBrokerDefinition {

    private final String brokerId;
    private final String host;
    private final int port;
    private final String clientId;
    private final String username;
    private final String password;
    private final boolean cleanSession;
    private final boolean sslEnabled;
    private final int keepAliveInterval;
    private final int connectionTimeout;
    private final ThreadPoolConfig inboundThreadPool;

    private MqttBrokerDefinition(Builder builder) {
        this.brokerId = requireText(builder.brokerId, "brokerId");
        this.host = requireText(builder.host, "host");
        this.port = builder.port;
        this.clientId = requireText(builder.clientId, "clientId");
        this.username = builder.username;
        this.password = builder.password;
        this.cleanSession = builder.cleanSession;
        this.sslEnabled = builder.sslEnabled;
        this.keepAliveInterval = builder.keepAliveInterval;
        this.connectionTimeout = builder.connectionTimeout;
        this.inboundThreadPool = Objects.requireNonNull(builder.inboundThreadPool, "inboundThreadPool must not be null");
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than 0");
        }
        if (keepAliveInterval <= 0) {
            throw new IllegalArgumentException("keepAliveInterval must be greater than 0");
        }
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("connectionTimeout must be greater than 0");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBrokerId() {
        return brokerId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public ThreadPoolConfig getInboundThreadPool() {
        return inboundThreadPool;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public static final class Builder {

        private String brokerId;
        private String host;
        private int port = 1883;
        private String clientId;
        private String username;
        private String password;
        private boolean cleanSession = true;
        private boolean sslEnabled;
        private int keepAliveInterval = 60;
        private int connectionTimeout = 30;
        private ThreadPoolConfig inboundThreadPool = ThreadPoolConfig.builder().build();

        public Builder brokerId(String brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public Builder sslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }

        public Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder inboundThreadPool(ThreadPoolConfig inboundThreadPool) {
            this.inboundThreadPool = inboundThreadPool;
            return this;
        }

        public MqttBrokerDefinition build() {
            return new MqttBrokerDefinition(this);
        }
    }
}
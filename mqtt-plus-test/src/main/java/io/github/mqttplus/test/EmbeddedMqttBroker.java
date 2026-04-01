package io.github.mqttplus.test;

import io.moquette.broker.Server;
import io.moquette.broker.config.MemoryConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EmbeddedMqttBroker implements AutoCloseable {

    private final Server server;
    private final String host;
    private final int port;
    private final AtomicBoolean started = new AtomicBoolean();

    private EmbeddedMqttBroker(Server server, String host, int port) {
        this.server = server;
        this.host = host;
        this.port = port;
        this.started.set(true);
    }

    public static EmbeddedMqttBroker startDefault() {
        String host = "127.0.0.1";
        int port = findAvailablePort();
        Properties properties = new Properties();
        properties.setProperty("host", host);
        properties.setProperty("port", Integer.toString(port));
        properties.setProperty("allow_anonymous", "true");
        properties.setProperty("persistence_enabled", "false");

        Server server = new Server();
        try {
            server.startServer(new MemoryConfig(properties));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start embedded MQTT broker", ex);
        }
        return new EmbeddedMqttBroker(server, host, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isStarted() {
        return started.get();
    }

    public void stop() {
        if (started.compareAndSet(true, false)) {
            server.stopServer();
        }
    }

    @Override
    public void close() {
        stop();
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to allocate port for embedded MQTT broker", ex);
        }
    }
}
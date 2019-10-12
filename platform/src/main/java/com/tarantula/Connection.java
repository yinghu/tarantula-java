package com.tarantula;

public interface Connection extends Response {

    String WEB_SOCKET = "websocket";
    String UDP = "udp";

    String type();
    void type(String type);

    String serverId();
    void serverId(String serverId);

    boolean secured();
    void secured(boolean secured);

    String protocol();
    void protocol(String protocol);

    String host();
    void host(String host);

    int port();
    void port(int port);

    String path();
    void path(String path);

    int maxConnections();
    void maxConnections(int maxConnections);


    interface Listener{
        void onState(Connection connection);
    }
}

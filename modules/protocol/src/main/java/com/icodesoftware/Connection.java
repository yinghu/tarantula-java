package com.icodesoftware;

public interface Connection extends Configurable {

    String WEB_HOOK = "webhook";
    String UDP = "udp";
    String WEB_SOCKET = "websocket";

    String type();
    void type(String type);

    String serverId();
    void serverId(String serverId);

    boolean secured();
    void secured(boolean secured);

    String protocol();
    void protocol(String protocol);

    String subProtocol();
    void subProtocol(String subProtocol);

    String host();
    void host(String host);

    int port();
    void port(int port);

    String path();
    void path(String path);


    interface OnStateListener{
        String typeId();
        void onState(Connection connection);
    }
    interface OnConnectionListener{
        String lobbyTag();
        void onConnection(Session session);
    }
}

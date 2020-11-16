package com.icodesoftware;

public interface Connection extends Recoverable {

    String WEB_HOOK = "webhook";
    String UDP = "udp";
    String WEB_SOCKET = "websocket";

    String type();
    void type(String type);

    String serverId();
    void serverId(String serverId);

    long connectionId();
    void connectionId(long connectionId);

    int sessionId();
    void sessionId(int sessionId);

    int sequence();
    void sequence(int sequence);

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

    int maxConnections();
    void maxConnections(int maxConnections);

    Connection server();
    void server(Connection connection);

    //default void registerInboundMessageListener(InboundMessageListener listener){}
    //default void update(int code,byte[] payload){}

    interface Listener{
        String typeId();
        void onState(Connection connection);
    }
    //interface InboundMessageListener{
        //void onUpdated(int code,byte[] updated);
    //}
}

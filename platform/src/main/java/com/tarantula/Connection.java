package com.tarantula;

public interface Connection extends Response {

    String WEB_HOOK = "webhook";
    String UDP = "udp";

    String type();
    void type(String type);

    String serverId();
    void serverId(String serverId);

    long sequence();
    void sequence(long sequence);

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

    default void registerInboundListener(InboundListener listener){}
    default void update(byte[] payload){}

    interface Listener{
        String typeId();
        void onState(Connection connection);
    }
    interface InboundListener{
        void onUpdated(byte[] updated);
        //void onEnded(byte[] ended);
    }
}

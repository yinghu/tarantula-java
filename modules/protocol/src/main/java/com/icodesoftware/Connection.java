package com.icodesoftware;

public interface Connection extends Configurable {


    String UDP = "udp";

    String type();
    void type(String type);

    int timeout();
    void timeout(int timeout);

    String serverId();
    void serverId(String serverId);

    boolean secured();
    void secured(boolean secured);

    String host();
    void host(String host);

    int port();
    void port(int port);

}

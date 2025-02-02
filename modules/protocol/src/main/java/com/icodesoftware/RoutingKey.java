package com.icodesoftware;

public interface RoutingKey {
    String bucket();
    String source();
    int routingNumber();
    String route();
    default String protocol(){
        return "";
    }
    default String host(){
        return "";
    }
    default int port(){
        return 0;
    }
}

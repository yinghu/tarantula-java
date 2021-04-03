package com.icodesoftware;

public interface RoutingKey {
    String bucket();
    String source();
    int routingNumber();
    String route();
}

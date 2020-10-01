package com.icodesoftware;


/**
 * Updated by yinghu lu on 8/9/2019.
 */
public interface RoutingKey {
    String bucket();
    String source();
    int routingNumber();
    String route();
}

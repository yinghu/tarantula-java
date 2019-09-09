package com.tarantula.integration.udp;

public class Main {
    public static void main(String[] args) throws Exception{
        ServiceConnector serviceConnector = new ServiceConnector();
        serviceConnector.start();
        new Thread(serviceConnector).start();
    }
}

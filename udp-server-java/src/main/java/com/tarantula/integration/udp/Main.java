package com.tarantula.integration.udp;

public class Main {
    static {
        //System.setProperty("java.util.logging.config.file","logging.properties");
    }
    public static void main(String[] args) throws Exception{
        ServiceConnector serviceConnector = new ServiceConnector();
        serviceConnector.start();
        new Thread(serviceConnector,"tarantula-udp-connector").start();
    }
}

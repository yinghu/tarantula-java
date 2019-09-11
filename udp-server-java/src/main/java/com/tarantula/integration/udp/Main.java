package com.tarantula.integration.udp;

import java.util.logging.LogManager;

public class Main {
    static {
        try {
            LogManager.getLogManager().readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception{
        ServiceConnector serviceConnector = new ServiceConnector();
        serviceConnector.start();
        new Thread(serviceConnector,"tarantula-udp-connector").start();
    }
}

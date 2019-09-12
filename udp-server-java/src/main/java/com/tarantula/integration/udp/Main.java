package com.tarantula.integration.udp;

import java.util.logging.LogManager;

public class Main {
    static {
        try {
            LogManager.getLogManager().readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static void main(String[] args) throws Exception{
        ServiceConnector serviceConnector = new ServiceConnector();
        serviceConnector.start();
        Thread tm = new Thread(serviceConnector,"tarantula-udp-connector");
        tm.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            serviceConnector.stop();
            tm.interrupt();
        },"shutdown-hook"));
    }
}

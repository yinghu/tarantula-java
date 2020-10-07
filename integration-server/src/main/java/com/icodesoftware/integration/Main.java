package com.icodesoftware.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.logging.TarantulaLogManager;
import com.icodesoftware.integration.udp.UDPService;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Main {
    static {//set log manager
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    public static void main(String[] args) throws Exception{
        JsonParser jsonParser = new JsonParser();
        File f = new File("/etc/tarantula/udp.conf");
        JsonObject config;
        if(f.exists()){
            config = jsonParser.parse(new InputStreamReader(new FileInputStream(f))).getAsJsonObject();
        }
        else{
            config = jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("udp.conf"))).getAsJsonObject();
        }
        UDPService udpReceiver = new UDPService(config);
        udpReceiver.start();
        Thread t = new Thread(udpReceiver,"udp-service");
        t.start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try{udpReceiver.shutdown();}catch (Exception ex){}
            TarantulaLogManager.shutdown();//shutdown log manager
        }));
    }
}

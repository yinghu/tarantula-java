package com.icodesoftware.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.integration.udp.UDPService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.LogManager;

public class Main {
    static {
        try {
            Properties _props = new Properties();
            _props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
            String[] fid = _props.getProperty("java.util.logging.FileHandler.pattern").split("/");
            StringBuilder fs = new StringBuilder();
            fs.append(System.getProperty("user.home")).append(FileSystems.getDefault().getSeparator());
            for(int i=1;i<fid.length-1;i++){
                fs.append(fid[i]).append(FileSystems.getDefault().getSeparator());
            }
            Path _path = Paths.get(fs.substring(0,fs.length()-1));
            if(!Files.exists(_path)){
                Files.createDirectories(_path);
            }
            LogManager.getLogManager().readConfiguration(Thread.currentThread().getContextClassLoader().getResourceAsStream("logging.properties"));
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
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
        }));
    }
}

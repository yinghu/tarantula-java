package com.tarantula.integration.udp;

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

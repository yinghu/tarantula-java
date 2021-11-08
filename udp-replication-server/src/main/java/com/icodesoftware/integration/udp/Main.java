package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.logging.TarantulaLogManager;

import java.io.*;

public class Main {
    static {//set log manager
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    public static void main(String[] args){
        try{
            JsonParser jsonParser = new JsonParser();
            File f = new File("/etc/tarantula/udp.conf");
            JsonObject config;
            //if(f.exists()){
                //config = jsonParser.parse(new InputStreamReader(new FileInputStream(f))).getAsJsonObject();
            //}
            //else{
                config = jsonParser.parse(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("udp.conf"))).getAsJsonObject();
            //}
            try{
                File fp = new File("/etc/tarantula/ip.txt");
                if(fp.exists()){
                    //read line
                    BufferedReader reader = new BufferedReader(new FileReader(fp));
                    String endpointIp = reader.readLine();
                    config.getAsJsonObject("connection").addProperty("host",endpointIp);
                    reader.close();
                }
            }catch (Exception ex){
                //throw new RuntimeException("No endpoint IP found from /etc/tarantula/ip.txt");
            }
            ReplicationEndpoint replicationEndpoint = new ReplicationEndpoint(config);
            replicationEndpoint.start();
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try{
                    replicationEndpoint.shutdown();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                TarantulaLogManager.shutdown();//shutdown log manager
            }));
        }catch (Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

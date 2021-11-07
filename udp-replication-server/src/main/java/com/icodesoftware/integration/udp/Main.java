package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.logging.TarantulaLogManager;

import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.util.HttpCaller;

import java.io.*;

public class Main {
    static {//set log manager
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    public static void main(String[] args){
        try{
            TarantulaLogger logger = JDKLogger.getLogger(Main.class);
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
            UDPEndpointServiceProvider udpReceiver = (UDPEndpointServiceProvider)Class.forName(config.get("endpointServiceProvider").getAsString()).getConstructor().newInstance();
            ReplicationEndpoint replicationEndpoint = new ReplicationEndpoint(udpReceiver,config.get("daemon").getAsBoolean());
            replicationEndpoint.address(config.get("binding").getAsString());
            replicationEndpoint.port(config.get("port").getAsInt());
            replicationEndpoint.inboundThreadPoolSetting(config.get("inboundThreadPoolSetting").getAsString());
            JsonObject register = config.getAsJsonObject("register");
            HttpCaller httpCaller = new HttpCaller(register.get("url").getAsString());
            httpCaller._init();
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY,
                    register.get("accessKey").getAsString(),
                    Session.TARANTULA_SERVER_ID,
                    "SERVERid",
                    Session.TARANTULA_ACTION,
                    "onStart"
            };
            String resp = httpCaller.get(register.get("path").getAsString(),headers);
            logger.warn(resp);
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try{replicationEndpoint.shutdown();}catch (Exception ex){}
                TarantulaLogManager.shutdown();//shutdown log manager
            }));
            replicationEndpoint.start();
        }catch (Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

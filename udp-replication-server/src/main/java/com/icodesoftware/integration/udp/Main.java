package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.logging.TarantulaLogManager;

import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.util.HttpCaller;

import java.io.*;
import java.util.UUID;

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
            ReplicationEndpoint replicationEndpoint = new ReplicationEndpoint(udpReceiver, UUID.randomUUID().toString(),config.get("daemon").getAsBoolean());
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
                    replicationEndpoint.serverId,
                    Session.TARANTULA_ACTION,
                    "onStart"
            };
            JsonObject conn = config.getAsJsonObject("connection");
            conn.addProperty("serverId",replicationEndpoint.serverId);
            String resp = httpCaller.post(register.get("path").getAsString(),conn.toString().getBytes(),headers);
            logger.warn(resp);
            for(int i=0;i<100;i++){
                JsonObject channel = new JsonObject();
                channel.addProperty("channelId",i+1);
                channel.addProperty("sessionId",i+1);
                headers[5]="onChannel";
                resp = httpCaller.post(register.get("path").getAsString(),channel.toString().getBytes(),headers);
                logger.warn(resp);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try{
                    headers[5]="onStop";
                    String _resp = httpCaller.post(register.get("path").getAsString(),conn.toString().getBytes(),headers);
                    logger.warn(_resp);
                    replicationEndpoint.shutdown();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                TarantulaLogManager.shutdown();//shutdown log manager
            }));
            replicationEndpoint.start();
        }catch (Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarantula.test.HTTPCaller;
import com.tarantula.test.UDPListener;

import java.net.http.WebSocket;

public class DemoSync extends OnGame {

    private JsonParser parser;
    private UDPListener udpListener;
    public DemoSync(){
        super();
        parser = new JsonParser();
    }

    @Override
    public String typeId() {
        return "demo";
    }

    @Override
    public void onPlay(JsonObject joined, WebSocket webSocket, HTTPCaller caller, JsonObject presence){
        try{
            if(!joined.get("successful").getAsBoolean()){
                return;
            }
            System.out.println(joined.toString());
            JsonObject conn = joined.get("gameObject").getAsJsonObject().get("connection").getAsJsonObject();
            String ticket = joined.get("gameObject").getAsJsonObject().get("ticket").getAsString();
            this.presence = presence;
            udpListener = new UDPListener(data -> {
                LoadResult.totalBytesUDPReceived.addAndGet(data.length);
                System.out.println(new String(data));
                /**
                StringBuilder label = new StringBuilder();
                for(byte b : data){
                    if(((char)b=='{')){
                        break;
                    }
                    else{
                        label.append((char)b);
                    }
                }
                if(label.toString().equals("ticket")){
                    System.out.println(new String(data));
                }**/
                //buff.append((char) data[0]).append((char) data[1]).append((char) data[2]).append((char) data[3]).append((char) data[4]).append((char)data[5]);
                //if(buff.toString().equals("ticket")){
                    //System.out.println(new String(data));
                //}
            });
            Thread.sleep(5000);
            udpListener.connect(conn.get("host").getAsString(),conn.get("port").getAsInt());
            udpListener.join(this.presence.get("login").getAsString(),this.presence.get("stub").getAsInt(),joined.get("instanceId").getAsString(),ticket);
            Thread tx = new Thread(udpListener);
            tx.start();
            this.applicationId = joined.get("applicationId").getAsString();
            this.instanceId = joined.get("instanceId").getAsString();
            long waiting = 250;
            //onStream(webSocket);
            for(int i=0;i<10;i++){
                onAction(webSocket,data->{data.addProperty("command","a");data.addProperty("timestamp",System.currentTimeMillis());});
                udpListener.message(joined);
                Thread.sleep(waiting);
                onAction(webSocket,data->{data.addProperty("command","b");data.addProperty("timestamp",System.currentTimeMillis());});
                Thread.sleep(waiting);
                onAction(webSocket,data->{data.addProperty("command","c");data.addProperty("timestamp",System.currentTimeMillis());});
                Thread.sleep(waiting);
            }
            Thread.sleep(5000);
            udpListener.leave(presence.get("systemId").getAsString(),joined.get("instanceId").getAsString());
            tx.interrupt();
            onAction(caller,data ->data.addProperty("command","onLeave"));
            System.out.println(LoadResult.print());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void onMessage(CharSequence message){
        super.onMessage(message);
        /**
        if(message.charAt(4)=='{'){
            JsonObject jo = parser.parse(message.subSequence(4,message.length()).toString()).getAsJsonObject();
            if(jo.has("command")){
                String cmd = jo.get("command").getAsString();
                if(cmd.equals("a")||cmd.equals("b")||cmd.equals("c")){
                    long dur = (System.currentTimeMillis()-jo.get("timestamp").getAsLong());
                    if(dur<=10){
                        LoadResult.totalRoundTrip1_10.incrementAndGet();
                    }
                    else if(dur>10&&dur<=50){
                        LoadResult.totalRoundTrip11_50.incrementAndGet();
                    }
                    else if(dur>50&&dur<=100){
                        LoadResult.totalRoundTrip51_100.incrementAndGet();
                    }
                    else if(dur>100&&dur<=500){
                        LoadResult.totalRoundTrip101_500.incrementAndGet();
                    }
                    else{
                        LoadResult.totalRoundTripMore500.incrementAndGet();
                    }
                }
            }
        }**/
    }
}

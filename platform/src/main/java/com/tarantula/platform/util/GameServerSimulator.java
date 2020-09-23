package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarantula.Connection;
import com.tarantula.Session;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yinghu lu on 9/19/2020.
 */
public class GameServerSimulator {
    static HttpCaller caller;
    static String serverId;
    static JsonParser parser;
    static String accessKey = "BDS01/81280cec10d244d5a324d5fcb211fdcd-75596F4EB936FFF376D31E26D5F204F48E23C921";
    static DatagramChannel datagramChannel;

    public static void main(String[] args) throws Exception{
        datagramChannel = DatagramChannel.open();
        datagramChannel.bind(new InetSocketAddress("10.0.0.234",16393));
        CountDownLatch ct = new CountDownLatch(1);
        Thread t = new Thread(()->{
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                SocketAddress sc = datagramChannel.receive(buffer);
                System.out.println(sc.toString()+""+new String(buffer.array()).trim());
                datagramChannel.send(ByteBuffer.wrap("popop".getBytes()),sc);

            }catch (Exception ex){
                ex.printStackTrace();
            }
            ct.countDown();
        });
        t.start();
        parser = new JsonParser();
        serverId = UUID.randomUUID().toString();
        caller = new HttpCaller("http://10.0.0.234:8090");
        caller._init();
        caller.index();
        JsonObject resp = parser.parse(onStart(accessKey)).getAsJsonObject();
        System.out.println(resp);
        ct.await();
        //onStop(accessKey);
    }

    static String onStart(String accessKey) throws Exception{
        JsonObject json = new JsonObject();
        json.addProperty("serverId",serverId);
        json.addProperty("host","10.0.0.234");
        json.addProperty("port",16393);
        json.addProperty("type", Connection.UDP);
        json.addProperty("maxConnections",10);
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,accessKey,
                Session.TARANTULA_ACTION,"onStart",
                Session.TARANTULA_SERVER_ID,serverId
        };
        return caller.post("server",json.toString().getBytes(),headers);
    }
    static String onStop(String accessKey) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,accessKey,
                Session.TARANTULA_ACTION,"onStop",
                Session.TARANTULA_SERVER_ID,serverId
        };
        return caller.get("server",headers);
    }
    static String key(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}

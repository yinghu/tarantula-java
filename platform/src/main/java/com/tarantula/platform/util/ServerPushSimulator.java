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
public class ServerPushSimulator {
    static HttpCaller caller;
    static String serverId;
    static JsonParser parser;
    static String udpHost="10.0.0.234";
    static String host = "http://10.0.0.234:8090";
    //static String accessKey = "BDS01/106c0e870f324829a432a31e3a94adba-4E2AC1EC9580C6B07239887AF6936A6698944B6B";
    static String accessKey = "BDS01/0794911cd333453f9ff3660e58dc427b-31CE1E59CA8F83407C318EC439F1817B0D59BD01";
    static DatagramChannel datagramChannel;

    public static void main(String[] args) throws Exception{
        datagramChannel = DatagramChannel.open();
        datagramChannel.bind(new InetSocketAddress(udpHost,16393));
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
        caller = new HttpCaller(host);
        caller._init();
        caller.index();
        JsonObject resp = parser.parse(onStart(accessKey)).getAsJsonObject();
        System.out.println(resp);
        ct.await();
        Thread.sleep(3000);
        onStop(accessKey);
    }

    static String onStart(String accessKey) throws Exception{
        JsonObject json = new JsonObject();
        json.addProperty("serverId",serverId);
        json.addProperty("host",udpHost);
        json.addProperty("port",16393);
        json.addProperty("type", Connection.UDP);
        json.addProperty("maxConnections",10);
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,accessKey,
                Session.TARANTULA_ACTION,"onStart",
                Session.TARANTULA_SERVER_ID,serverId
        };
        return caller.post("push",json.toString().getBytes(),headers);
    }
    static String onStop(String accessKey) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,accessKey,
                Session.TARANTULA_ACTION,"onStop",
                Session.TARANTULA_SERVER_ID,serverId
        };
        return caller.get("push",headers);
    }
}

package com.tarantula.platform.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Connection;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.util.HttpCaller;

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


public class GameServerSimulator {
    static HttpCaller caller;
    static String serverId;
    static JsonParser parser;
    static String accessKey = "BDS01/ee71a0f14f344cfda8f34974df958743-F0E3A7B7B711608C9C4F0A17E2006EDA8AA3100E";
    //static String accessKey = "BDS01/74853109aa094806a796daac974af5f6-80AA94233D78FCC5D001E45BF23A05A8BE940166";
    static DatagramChannel datagramChannel;

    public static void main(String[] args) throws Exception{
        datagramChannel = DatagramChannel.open();
        datagramChannel.bind(new InetSocketAddress("10.0.0.234",16393));
        CountDownLatch ct = new CountDownLatch(1);
        Thread t = new Thread(()->{
            try {
                for(int i=0;i<100;i++) {
                    ByteBuffer buffer = ByteBuffer.allocate(OutboundMessage.MESSAGE_SIZE);
                    SocketAddress sc = datagramChannel.receive(buffer);
                    InboundMessage pendingInboundMessage = new InboundMessage("", buffer,sc);
                    System.out.println(sc.toString() + "" + new String(pendingInboundMessage.payload()));

                    OutboundMessage out = new OutboundMessage();
                    out.sequence(pendingInboundMessage.sequence());
                    out.payload("killer".getBytes());
                    out.connectionId(pendingInboundMessage.connectionId());
                    //datagramChannel.send(out.message(),sc);
                }

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
        Thread.sleep(3000);
        onStop(accessKey);
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

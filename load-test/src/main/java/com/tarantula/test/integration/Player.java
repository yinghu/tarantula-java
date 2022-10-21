package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;

import javax.crypto.Cipher;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class Player extends HttpCaller implements Runnable{

    private CountDownLatch counter;
    private String userName;
    private String deviceName;
    private String clientId;
    private String token;
    private String ticket;

    //private byte[] serverKey;
    private Cipher encryper;
    private DatagramSocket udp;
    //private OnPayload done;
    //private boolean[] continuing = {true};
    private JsonObject presence;
    private JsonObject connection;

    private CountDownLatch waiting;


    private long start;
    private JsonObject gameLobby;
    private StringBuilder dataBuffer;
    CompletableFuture<?> accumulatedMessage;
    public Player(String host, CountDownLatch counter, String userName,int sequence){
        super(host);
        this.counter = counter;
        this.userName = userName;
        this.deviceName = "test-"+sequence;
        this.clientId = UUID.randomUUID().toString();
        //this.done = done;
        waiting = new CountDownLatch(1);
        this.dataBuffer = new StringBuilder();
        this.accumulatedMessage = new CompletableFuture<>();
    }
    public void _init(){
        try{
            super._init();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private boolean isContinue(String payload,OnPayload onPayload){
        JsonObject json = JsonUtil.parse(payload);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) return false;
        onPayload.on(json);
        return true;
    }
    public void run() {
        start = System.currentTimeMillis();
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"index/user",
                    Session.TARANTULA_ACTION,"onRegister",
                    Session.TARANTULA_MAGIC_KEY,userName
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("login",userName);
            jsonObject.addProperty("password","password");
            String resp = super.post("user/action",jsonObject.toString().getBytes(),headers);
            if(isContinue(resp,json->{
                token = json.get("Token").getAsString();
                ticket = json.get("Ticket").getAsString();
            })){
                headers = new String[]{
                        Session.TARANTULA_TAG,"robotquest/lobby",
                        Session.TARANTULA_ACTION,"onPlay",
                        Session.TARANTULA_TOKEN,token,
                        Session.TARANTULA_NAME,deviceName,
                        Session.TARANTULA_CLIENT_ID,clientId,
                        Session.TARANTULA_TOURNAMENT_ID,"n/a"
                };
                resp = super.get("service/action",headers);
                if(isContinue(resp,json->{
                    JsonObject _channel = json.get("_pushChannel").getAsJsonObject();
                    try{onPlay(_channel);}catch (Exception exx){
                        exx.printStackTrace();
                    }
                }));
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            counter.countDown();
        }
    }



    private void onPlay(JsonObject json) throws Exception{
        byte[] serverKey = Base64.getDecoder().decode(json.get("ServerKey").getAsString());
        this.encryper = CipherUtil.encrypt(serverKey);
        udp = new DatagramSocket();
        int channelId = json.get("ChannelId").getAsInt();
        int sessionId = json.get("SessionId").getAsInt();
        JsonObject _conn = json.get("_connection").getAsJsonObject();
        String host = _conn.get("Host").getAsString();
        int port = _conn.get("Port").getAsInt();
        System.out.println(json);
        InetAddress addr = InetAddress.getByName(host);
        udp.connect(new InetSocketAddress(addr,port));
        MessageBuffer.MessageHeader header = new MessageBuffer.MessageHeader();
        header.channelId = channelId;
        header.sessionId = sessionId;
        header.commandId = Messenger.JOIN;
        header.encrypted = true;
        MessageBuffer messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(header);
        messageBuffer.writeInt(sessionId);
        messageBuffer.writeUTF8(token);
        messageBuffer.writeUTF8(ticket);
        messageBuffer.flip();
        messageBuffer.readHeader();
        byte[] payload = this.encryper.doFinal(messageBuffer.readPayload());
        messageBuffer.reset();
        messageBuffer.writeHeader(header);
        messageBuffer.writePayload(payload);
        messageBuffer.flip();
        byte[] out = messageBuffer.toArray();
        //messageBuffer.reset();
        //MessageBuffer moo = new MessageBuffer();

        //System.out.println("Connected->"+udp.isConnected());
        udp.send(new DatagramPacket(out,0,out.length));
        //System.out.println("UDP-send-"+out.length);
        DatagramPacket rec = new DatagramPacket(new byte[MessageBuffer.PAYLOAD_SIZE],MessageBuffer.PAYLOAD_SIZE);
        udp.receive(rec);
        System.out.println("UDP-received");
        messageBuffer.reset(rec.getData());
        messageBuffer.flip();
        MessageBuffer.MessageHeader h = messageBuffer.readHeader();
        System.out.println(messageBuffer.readInt());
        System.out.println(messageBuffer.readLong());
        System.out.println(h.commandId);

    }
}

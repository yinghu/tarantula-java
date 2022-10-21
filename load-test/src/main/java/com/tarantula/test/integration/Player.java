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
import java.util.concurrent.CountDownLatch;

public class Player extends HttpCaller implements Runnable{

    private CountDownLatch counter;
    private String userName;
    private String deviceName;
    private String clientId;
    private String token;
    private String ticket;

    private Cipher encryper;
    private DatagramSocket udp;
    private MessageBuffer messageBuffer;
    private MessageBuffer.MessageHeader messageHeader;

    private int udpReceiveTimeout = 3000; //udp receive timeout 3 secs
    private long duration = 10000; //total running time

    private boolean joined;
    public Player(String host, CountDownLatch counter, String userName,int sequence){
        super(host);
        this.counter = counter;
        this.userName = userName;
        this.deviceName = "test-"+sequence;
        this.clientId = UUID.randomUUID().toString();
    }

    private boolean onPresence(String payload){
        JsonObject json = JsonUtil.parse(payload);
        boolean suc = json.get("Successful").getAsBoolean();
        if(!suc) return false;
        token = json.get("Token").getAsString();
        ticket = json.get("Ticket").getAsString();
        return true;
    }
    private void join() {
        if(joined) return;
        try{
            _init();
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"index/user",
                    Session.TARANTULA_ACTION,"onRegister",
                    Session.TARANTULA_MAGIC_KEY,userName
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("login",userName);
            jsonObject.addProperty("password","password");
            String resp = super.post("user/action",jsonObject.toString().getBytes(),headers);
            if(!onPresence(resp)) {
                LoadResult.totalFailurePresence.incrementAndGet();
                throw new RuntimeException("failed");
            }
            LoadResult.totalSuccessPresence.incrementAndGet();
            headers = new String[]{
                    Session.TARANTULA_TAG,"robotquest/lobby",
                    Session.TARANTULA_ACTION,"onPlay",
                    Session.TARANTULA_TOKEN,token,
                    Session.TARANTULA_NAME,deviceName,
                    Session.TARANTULA_CLIENT_ID,clientId,
                    Session.TARANTULA_TOURNAMENT_ID,"n/a"
            };
            resp = super.get("service/action",headers);
            onJoin(resp);
            joined = true;
        }catch (Exception ex){
            ex.printStackTrace();
            String error = ex.getMessage();
            if(!error.equals("failed")){
                LoadResult.totalFailureOther.incrementAndGet();
            }
            duration = 0;
            counter.countDown();
            joined = false;
        }
    }

    public void run(){
        join();
        try{
            while (duration>0){
                Thread.sleep(5000);
                duration -= 5000;
            }
            LoadResult.totalRounds.incrementAndGet();
            counter.countDown();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void onJoin(String resp) throws Exception{
        JsonObject joinPayload = JsonUtil.parse(resp);
        boolean suc = joinPayload.get("Successful").getAsBoolean();
        if(!suc){
            LoadResult.totalFailureJoin.incrementAndGet();
            throw new RuntimeException("failed");
        }
        LoadResult.totalSuccessJoin.incrementAndGet();
        JsonObject channel = joinPayload.get("_pushChannel").getAsJsonObject();
        byte[] serverKey = Base64.getDecoder().decode(channel.get("ServerKey").getAsString());
        this.encryper = CipherUtil.encrypt(serverKey);
        udp = new DatagramSocket();
        udp.setSoTimeout(udpReceiveTimeout);
        int channelId = channel.get("ChannelId").getAsInt();
        int sessionId = channel.get("SessionId").getAsInt();
        JsonObject _conn = channel.get("_connection").getAsJsonObject();
        String host = _conn.get("Host").getAsString();
        int port = _conn.get("Port").getAsInt();
        InetAddress addr = InetAddress.getByName(host);
        udp.connect(new InetSocketAddress(addr,port));
        messageHeader = new MessageBuffer.MessageHeader();
        messageHeader.channelId = channelId;
        messageHeader.sessionId = sessionId;
        messageHeader.commandId = Messenger.JOIN;
        messageHeader.encrypted = true;
        messageBuffer = new MessageBuffer();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(sessionId);
        messageBuffer.writeUTF8(token);
        messageBuffer.writeUTF8(ticket);
        messageBuffer.flip();
        messageBuffer.readHeader();
        byte[] payload = this.encryper.doFinal(messageBuffer.readPayload());
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writePayload(payload);
        messageBuffer.flip();
        byte[] outbound = messageBuffer.toArray();
        udp.send(new DatagramPacket(outbound,0,outbound.length));
        LoadResult.totalUDPBytesSent.addAndGet(outbound.length);
        DatagramPacket rec = new DatagramPacket(new byte[MessageBuffer.PAYLOAD_SIZE],MessageBuffer.PAYLOAD_SIZE);
        udp.receive(rec);
        byte[] inbound = rec.getData();
        LoadResult.totalUDPBytesReceived.addAndGet(inbound.length);
        messageBuffer.reset(inbound);
        messageBuffer.flip();
        MessageBuffer.MessageHeader h = messageBuffer.readHeader();
        int returnSessionId = messageBuffer.readInt();
        if(h.commandId == Messenger.ON_JOIN && returnSessionId == sessionId ){
            LoadResult.totalSuccessPlay.incrementAndGet();
            return;
        }
        LoadResult.totalFailurePlay.incrementAndGet();
        throw new RuntimeException("failed");
    }
}

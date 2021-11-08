package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.OnSession;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.ValidationUtil;

import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

public class ReplicationEndpoint implements Serviceable,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.RequestListener {

    TarantulaLogger logger = JDKLogger.getLogger(ReplicationEndpoint.class);

    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private byte[] serverKey;
    private String accessKey;
    private String typeId;
    private String serverId;
    private int maxChannelSize;
    private MessageDigest messageDigest;


    private HttpCaller httpCaller;
    private String registerPath;
    private JsonObject connection;

    private JsonObject config;

    public ReplicationEndpoint(JsonObject config){
        this.config = config;
    }


    @Override
    public void start() throws Exception {
        this.messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
        this.serverId = UUID.randomUUID().toString();
        this.udpEndpointServiceProvider = (UDPEndpointServiceProvider)Class.forName(config.get("endpointServiceProvider").getAsString()).getConstructor().newInstance();
        this.udpEndpointServiceProvider.address(config.get("binding").getAsString());
        this.udpEndpointServiceProvider.port(config.get("port").getAsInt());
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(config.get("inboundThreadPoolSetting").getAsString());
        boolean daemon = config.get("daemon").getAsBoolean();
        this.udpEndpointServiceProvider.daemon(daemon);
        this.udpEndpointServiceProvider.start();
        JsonObject register = config.getAsJsonObject("register");
        this.accessKey = register.get("accessKey").getAsString();
        this.registerPath = register.get("path").getAsString();
        this.maxChannelSize = config.get("maxChannelSize").getAsInt();
        this.connection = config.getAsJsonObject("connection");
        this.httpCaller = new HttpCaller(register.get("url").getAsString());
        this.httpCaller._init();
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,
                accessKey,
                Session.TARANTULA_SERVER_ID,
                serverId,
                Session.TARANTULA_ACTION,
                "onStart"
        };
        connection.addProperty("serverId",serverId);
        String resp = httpCaller.post(register.get("path").getAsString(),connection.toString().getBytes(),headers);
        JsonObject jo = JsonUtil.parse(resp);
        if(!jo.get("successful").getAsBoolean()) throw new RuntimeException(resp);
        this.serverKey = Base64.getDecoder().decode(jo.get("serverKey").getAsString());
        this.typeId = jo.get("typeId").getAsString();
        for(int i=1;i<=maxChannelSize;i++){
            JsonObject channel = new JsonObject();
            channel.addProperty("channelId",i);
            channel.addProperty("sessionId",i);
            headers[5]="onChannel";
            resp = httpCaller.post(register.get("path").getAsString(),channel.toString().getBytes(),headers);
            jo = JsonUtil.parse(resp);
            if(!jo.get("successful").getAsBoolean()) throw new RuntimeException(resp);
            udpEndpointServiceProvider.registerUserChannel(new GameUserChannel(i,udpEndpointServiceProvider,this,this,this));
        }
        logger.warn("Game server is running on ["+typeId+"]");
        if(!daemon) this.udpEndpointServiceProvider.run();
    }

    @Override
    public void shutdown() throws Exception {
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,
                accessKey,
                Session.TARANTULA_SERVER_ID,
                serverId,
                Session.TARANTULA_ACTION,
                "onStop"
        };
        httpCaller.post(registerPath,connection.toString().getBytes(),headers);
        this.udpEndpointServiceProvider.shutdown();
    }

    @Override
    public byte[] onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return null;
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        logger.warn("Session ["+sessionId+"] removed from ["+channelId+"]");
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
            if(!messageHeader.encrypted) return false;
            Cipher cipher = CipherUtil.decrypt(serverKey);
            byte[] plain = cipher.doFinal(messageBuffer.readPayload());
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(plain);
            messageBuffer.flip();
            messageBuffer.readHeader();
            int sessionId = messageBuffer.readInt();
            String token = messageBuffer.readUTF8();
            String ticket = messageBuffer.readUTF8();
            MessageDigest mda = (MessageDigest)messageDigest.clone();
            ValidationUtil.Token session = ValidationUtil.validToken(mda,token);
            boolean suc = ValidationUtil.validTicket(mda,session.systemId,session.stub,ticket);
            return sessionId==messageHeader.sessionId && suc;
        }catch (Exception ex){
            return false;
        }
    }
}

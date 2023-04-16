package com.tarantula.cci.udp;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.ChannelHeader;
import com.icodesoftware.util.CipherUtil;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Arrays;


public class GameChannel extends ChannelHeader implements Portable {


    public GameChannel(){

    }
    public GameChannel(int channelId,int sessionId,Connection connection,byte[] serverKey,int timeout){
        this.channelId = channelId;
        this.sessionId = sessionId;
        this.connection = connection;
        this.serverKey = serverKey;
        this.timeout = timeout;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.GAME_CHANNEL_CID;
    }
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",channelId);
        portableWriter.writeInt("2",sessionId);
        portableWriter.writeByteArray("3",serverKey);
        portableWriter.writePortable("4",(Portable) connection);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        channelId = portableReader.readInt("1");
        sessionId = portableReader.readInt("2");
        serverKey = portableReader.readByteArray("3");
        connection = portableReader.readPortable("4");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ChannelId",channelId);
        jsonObject.addProperty("SessionId",sessionId);
        jsonObject.addProperty("Timeout",timeout);
        jsonObject.addProperty("ServerKey", CipherUtil.toBase64Key(serverKey));
        jsonObject.add("_connection",connection.toJson());
        return jsonObject;
    }

    public void sessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(new int[]{channelId,sessionId});
    }
    @Override
    public boolean equals(Object obj){
        GameChannel r = (GameChannel)obj;
        return channelId == r.channelId() && sessionId == r.sessionId();
    }
}

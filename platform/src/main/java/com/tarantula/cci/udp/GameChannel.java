package com.tarantula.cci.udp;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;


public class GameChannel extends RecoverableObject implements Channel, Portable {

    protected String configurationTypeId;

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;


    public GameChannel(){

    }
    public GameChannel(int channelId,int sessionId,Connection connection,byte[] serverKey,int timeout){
        this.channelId = channelId;
        this.sessionId = sessionId;
        this.connection = connection;
        this.serverKey = serverKey;
        this.timeout = timeout;
    }
    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public void configurationTypeId(String configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
    }

    @Override
    public int channelId() {
        return channelId;
    }

    @Override
    public int sessionId() {
        return sessionId;
    }

    public int timeout(){
        return this.timeout;
    }
    public byte[] serverKey(){
        return serverKey;
    }

    @Override
    public void write(MessageBuffer.MessageHeader messageHeader, byte[] bytes) {

    }

    @Override
    public Connection connection() {
        return connection;
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
    public void close(){}
}

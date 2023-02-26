package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.MessageBuffer;


import java.io.IOException;

public class ServerPushEvent extends Data implements Event {

    private boolean ack;
    private int channelId;
    private int sessionId;
    private int objectId;
    private int sequence;
    private short commandId;
    private short batchSize;
    private short batch;
    private boolean broadcasting;
    private boolean encrypted;

    public Session.Header messageHeader(){
        MessageBuffer.MessageHeader header = new MessageBuffer.MessageHeader();
        header.ack = ack;
        header.channelId = channelId;
        header.sessionId = sessionId;
        header.objectId = objectId;
        header.sequence = sequence;
        header.commandId = commandId;
        header.batchSize = batchSize;
        header.batch = batch;
        header.broadcasting = broadcasting;
        header.encrypted = encrypted;
        return header;
    }

    public ServerPushEvent(){

    }

    public ServerPushEvent(String destination, String trackId, byte[] value){
        this.destination = destination;
        this.trackId = trackId;
        this.payload = value;
    }

    public ServerPushEvent(String destination, String trackId, Session.Header messageHeader, byte[] value){
        this.destination = destination;
        this.trackId = trackId;
        this.ack = messageHeader.ack();
        this.channelId = messageHeader.channelId();
        this.sessionId = messageHeader.sessionId();
        this.objectId = messageHeader.objectId();
        this.sequence = messageHeader.sequence();
        this.commandId = messageHeader.commandId();
        this.batchSize = messageHeader.batchSize();
        this.batch = messageHeader.batch();
        this.broadcasting = messageHeader.broadcasting();
        this.encrypted = messageHeader.encrypted();
        this.payload = value;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.trackId);
        out.writeBoolean("3",this.ack);
        out.writeInt("4",channelId);
        out.writeInt("5",sessionId);
        out.writeInt("6",objectId);
        out.writeInt("7",sequence);
        out.writeShort("8",commandId);
        out.writeShort("9",batchSize);
        out.writeShort("10",batch);
        out.writeBoolean("11",this.broadcasting);
        out.writeBoolean("12",this.encrypted);
        out.writeByteArray("13",this.payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.trackId = in.readUTF("2");
        this.ack = in.readBoolean("3");
        this.channelId = in.readInt("4");
        this.sessionId = in.readInt("5");
        this.objectId = in.readInt("6");
        this.sequence = in.readInt("7");
        this.commandId = in.readShort("8");
        this.batchSize = in.readShort("9");
        this.batch = in.readShort("10");
        this.broadcasting = in.readBoolean("11");
        this.encrypted = in.readBoolean("12");
        this.payload = in.readByteArray("13");
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.SERVER_PUSH_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "Server push event ->["+destination+"/"+trackId+"//"+stub+">>>"+new String(payload)+"]";
    }
}

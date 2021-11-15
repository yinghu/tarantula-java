package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionStub extends ClientConnection implements Portable {

    public byte[] serverKey;

    public ConcurrentLinkedDeque<ChannelStub> channelStubs;
    public int maxCapacity;
    private AtomicInteger sessionId;
    private AtomicLong pingSequence;
    private long lastPing;
    private int tries;

    public ConnectionStub(){

    }


    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.CONNECTION_STUB_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        super.writePortable(portableWriter);
        portableWriter.writeByteArray("sk",serverKey);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        super.readPortable(portableReader);
        serverKey = portableReader.readByteArray("sk");
    }
    public void addChannel(ChannelStub channelStub){
        if(channelStubs==null) {
            channelStubs = new ConcurrentLinkedDeque<>();
            sessionId = new AtomicInteger(1);
            pingSequence = new AtomicLong(0);
            lastPing = 0;
        }
        channelStubs.offer(channelStub);
    }
    public GameChannel gameChannel(){
        ChannelStub channelStub = channelStubs.poll();
        return channelStub!=null?new GameChannel(channelStub.channelId(),sessionId.getAndAdd(maxCapacity),clientConnection(),serverKey,channelStub.timeout()):null;
    }
    public void close(){
        channelStubs.clear();
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this) return true;
        return ((ConnectionStub)obj).serverId.equals(serverId);
    }
    @Override
    public int hashCode(){
        return serverId.hashCode();
    }
    private ClientConnection clientConnection(){
        ClientConnection clientConnection = new ClientConnection();
        clientConnection.host(host);
        clientConnection.port(port);
        clientConnection.type(type);
        clientConnection.protocol(protocol);
        clientConnection.subProtocol(subProtocol);
        clientConnection.secured(secured);
        clientConnection.path(path);
        return clientConnection;
    }
    public void ping(){
        pingSequence.incrementAndGet();
    }
    public boolean check(){
        long ping;
        if((ping=pingSequence.get())-lastPing>0){
            lastPing = ping;
            return true;
        }
        tries++;
        return tries<3;
    }

}
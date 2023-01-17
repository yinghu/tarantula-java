package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionStub extends ClientConnection {

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
    public Map<String,Object> toMap(){
        this.properties.put("1",host);
        this.properties.put("2",port);
        this.properties.put("3",serverId);
        this.properties.put("4",type);
        this.properties.put("5",secured);
        this.properties.put("6",configurationName);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.host = (String) properties.get("1");
        this.port = ((Number)properties.get("2")).intValue();
        this.serverId = (String) properties.get("3");
        this.type = (String) properties.get("4");
        this.secured = (boolean) properties.get("5");
        this.configurationName = (String) properties.get("6");
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
        channelStubs.offer(channelStub);
    }
    public void init(){
        channelStubs = new ConcurrentLinkedDeque<>();
        sessionId = new AtomicInteger(1);
        pingSequence = new AtomicLong(0);
        lastPing = 0;
    }
    public boolean removeChannel(ChannelStub channelStub){
        return channelStubs.remove(channelStub);
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
        clientConnection.secured(secured);
        return clientConnection;
    }
    public void ping(){
        pingSequence.incrementAndGet();
    }
    public boolean timeout(){
        long ping;
        if((ping = pingSequence.get()) - lastPing > 0){
            lastPing = ping;
            tries = 0;
            return false;
        }
        tries++;
        return tries > UDPEndpointServiceProvider.CONNECTION_HEALTHY_CHECK_RETRIES;
    }

    @Override
    public String toString(){
        return "cb->"+serverId+"//"+host+":"+port;
    }

}
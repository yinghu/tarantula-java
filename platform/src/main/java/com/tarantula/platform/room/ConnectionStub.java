package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;

import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.event.PortableEventRegistry;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionStub extends ClientConnection {

    public byte[] serverKey;

    public int maxCapacity;
    public AtomicBoolean started;
    private AtomicLong pingSequence;
    private long lastPing;
    private int tries;

    private int connectionTimeout;

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
        this.properties.put("7",timeout);
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
        this.timeout = ((Number)properties.get("7")).intValue();
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

    public void init(){
        this.pingSequence = new AtomicLong(0);
        this.lastPing = 0;
        this.connectionTimeout = timeout;
        this.started = new AtomicBoolean(false);
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

    public ClientConnection clientConnection(){
        ClientConnection clientConnection = new ClientConnection();
        clientConnection.host(host);
        clientConnection.port(port);
        clientConnection.type(type);
        clientConnection.secured(secured);
        clientConnection.timeout(timeout);
        return clientConnection;
    }
    public void ping(){
        pingSequence.incrementAndGet();
    }

    public boolean onTimeout(int delta){
        if(!started.get()) return false;
        connectionTimeout -= delta;
        if(connectionTimeout > 0) return false;
        connectionTimeout = timeout;
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
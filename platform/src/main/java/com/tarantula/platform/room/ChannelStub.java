package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

import com.tarantula.cci.udp.GameChannel;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class ChannelStub extends GameChannel implements Portable {

    public String serverId;

    public ChannelStub(){

    }
    public ChannelStub(int channelId){
        this.channelId = channelId;
    }
    public ChannelStub(int channelId,String serverId){
        this.channelId = channelId;
        this.serverId = serverId;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.CHANNEL_STUB_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeInt("1",channelId);
        portableWriter.writeUTF("2",serverId);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        channelId = portableReader.readInt("1");
        serverId = portableReader.readUTF("2");
    }
}
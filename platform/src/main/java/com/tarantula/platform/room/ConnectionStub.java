package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class ConnectionStub extends ClientConnection implements Portable {

    public byte[] serverKey;

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

    public GameChannel gameChannel(){
        return new GameChannel(1,1,this,serverKey);
    }
}
package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;

import java.io.IOException;


public class TransactionReplicationEvent extends Data implements Event {

    public Portable[] pendingLogs;
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",destination);
        portableWriter.writeUTF("2",source);
        portableWriter.writePortableArray("3",pendingLogs);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        destination = portableReader.readUTF("1");
        source = portableReader.readUTF("2");
        pendingLogs = portableReader.readPortableArray("3");
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TRANSACTION_REPLICATION_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

}

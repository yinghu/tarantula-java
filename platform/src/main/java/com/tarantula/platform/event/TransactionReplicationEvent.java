package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.lmdb.TransactionLog;


import java.io.IOException;


public class TransactionReplicationEvent extends Data implements Event {

    public Portable[] pendingLogs;
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("dest",destination);
        portableWriter.writePortableArray("logs",pendingLogs);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        destination = portableReader.readUTF("dest");
        pendingLogs = portableReader.readPortableArray("logs");
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

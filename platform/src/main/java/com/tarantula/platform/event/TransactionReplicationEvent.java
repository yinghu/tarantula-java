package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.tarantula.platform.service.persistence.TransactionLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionReplicationEvent extends Data implements Event {

    public List<TransactionLog> pendingLogs;
    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        int sz = pendingLogs.size();
        portableWriter.writeUTF("dest",destination);
        portableWriter.writeInt("sz",sz);
        for(int i=0;i<sz;i++){
            writeLog(portableWriter,pendingLogs.get(i),i);
        }
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        destination = portableReader.readUTF("dest");
        int sz = portableReader.readInt("sz");
        pendingLogs = new ArrayList<>();
        for(int i=0;i<sz;i++){
            pendingLogs.add(readLog(portableReader,i));
        }
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TRANSACTION_REPLICATION_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    private void writeLog(PortableWriter portableWriter,TransactionLog transactionLog,int index) throws IOException{
        portableWriter.writeBoolean("d"+index,transactionLog.deleting);
        portableWriter.writeInt("c"+index,transactionLog.scope);
        portableWriter.writeUTF("s"+index,transactionLog.source);
        portableWriter.writeLong("u"+index,transactionLog.updatingRevision);
        portableWriter.writeUTF("e"+index,transactionLog.edgeLabel);
        portableWriter.writeByteArray("k"+index,transactionLog.key);
        portableWriter.writeByteArray("v"+index,transactionLog.value);
        portableWriter.writeByteArray("g"+index,transactionLog.edgeKey);

    }
    private TransactionLog readLog(PortableReader portableReader,int index) throws IOException{
        TransactionLog log = new TransactionLog();
        log.deleting = portableReader.readBoolean("d"+index);
        log.scope = portableReader.readInt("c"+index);
        log.source = portableReader.readUTF("s"+index);
        log.updatingRevision = portableReader.readLong("u"+index);
        log.edgeLabel = portableReader.readUTF("e"+index);
        log.key = portableReader.readByteArray("k"+index);
        log.value = portableReader.readByteArray("v"+index);
        log.edgeKey = portableReader.readByteArray("g"+index);
        return log;
    }
}

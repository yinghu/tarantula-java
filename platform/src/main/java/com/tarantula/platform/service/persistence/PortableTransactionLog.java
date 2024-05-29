package com.tarantula.platform.service.persistence;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;


import java.io.IOException;

public class PortableTransactionLog extends RecoverableObject implements Portable {

    public TransactionLog transactionLog;

    public PortableTransactionLog(TransactionLog transactionLog){
        this.transactionLog = transactionLog;
    }

    public PortableTransactionLog(){

    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.TRANSACTION_LOG_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeBoolean("1",transactionLog.deleting);
        portableWriter.writeInt("2",transactionLog.scope);
        portableWriter.writeUTF("3",transactionLog.source);
        portableWriter.writeLong("4",transactionLog.updatingRevision);
        portableWriter.writeUTF("5",transactionLog.edgeLabel);
        portableWriter.writeByteArray("6",transactionLog.key);
        portableWriter.writeByteArray("7",transactionLog.value);
        portableWriter.writeByteArray("8",transactionLog.edgeKey);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        transactionLog = new TransactionLog();
        transactionLog.deleting = portableReader.readBoolean("1");
        transactionLog.scope = portableReader.readInt("2");
        transactionLog.source = portableReader.readUTF("3");
        transactionLog.updatingRevision = portableReader.readLong("4");
        transactionLog.edgeLabel = portableReader.readUTF("5");
        transactionLog.key = portableReader.readByteArray("6");
        transactionLog.value = portableReader.readByteArray("7");
        transactionLog.edgeKey = portableReader.readByteArray("8");
    }

    @Override
    public byte[] toBinary() {
        //DataBuffer buffer = BufferProxy.buffer()
        return new byte[0];
    }

    @Override
    public void fromBinary(byte[] payload) {

    }
}

package com.tarantula.platform;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.service.Batch;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class BatchData extends RecoverableObject implements Portable, Batch {

    public String batchId;
    public String batchKey;
    public int count;
    public int size;
    public byte[] payload;
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.batchId);
        out.writeUTF("2",this.batchKey);
        out.writeInt("3",this.count);
        out.writeInt("4",this.size);
        out.writeByteArray("5",payload);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.batchId = in.readUTF("1");
        this.batchKey = in.readUTF("2");
        this.count= in.readInt("3");
        this.size = in.readInt("4");
        this.payload = in.readByteArray("5");
    }


    @Override
    public int getFactoryId() {
       return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.BATCH_CID;
    }

    @Override
    public String batchId() {
        return this.batchId;
    }
    @Override
    public String batchKey() {
        return this.batchKey;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public byte[] payload() {
        return payload;
    }

}

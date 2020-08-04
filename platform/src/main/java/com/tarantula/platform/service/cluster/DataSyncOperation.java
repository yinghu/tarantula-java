package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class DataSyncOperation extends Operation {

    private String source;
    private int factoryId;
    private int classId;
    private byte[] key;
    private byte[] value;

    public DataSyncOperation() {
    }


    public DataSyncOperation(String source,int factoryId,int classId,byte[] key,byte[] value) {
        this.source = source;
        this.factoryId = factoryId;
        this.classId = classId;
        this.key = key;
        this.value = value;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cis = this.getService();
        cis.syncService(source,factoryId,classId,key,value);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(source);
        out.writeInt(factoryId);
        out.writeInt(classId);
        out.writeByteArray(key);
        out.writeByteArray(value);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.source = in.readUTF();
        this.factoryId = in.readInt();
        this.classId = in.readInt();
        this.key = in.readByteArray();
        this.value = in.readByteArray();
    }
}

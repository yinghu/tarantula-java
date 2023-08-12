package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.service.persistence.ReplicationData;

import java.io.IOException;
import java.util.Arrays;


public class DataStoreSyncBatchOperation extends Operation {

    private int length;
    private byte[][] keys;
    private byte[][] values;
    private String source;
    public DataStoreSyncBatchOperation() {
    }
    public DataStoreSyncBatchOperation(int length, byte[][] keys, byte[][] values,String source) {
        this.length = length;
        this.source = source;
        this.keys = new byte[this.length][];
        this.values = new byte[this.length][];
        for(int i=0;i<this.length;i++){
            this.keys[i]= Arrays.copyOf(keys[i],keys[i].length);
            this.values[i]= Arrays.copyOf(values[i],values[i].length);
        }
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        ReplicationData[] data = new ReplicationData[length];
        for(int i=0;i<length;i++){
            data[i]=new ReplicationData(source,keys[i],values[i]);
        }
        cds.replicateAsBatch(data);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(source);
        out.writeInt(length);
        for(int i=0;i<length;i++){
            out.writeByteArray(keys[i]);
            out.writeByteArray(values[i]);
        }
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        source = in.readUTF();
        this.length = in.readInt();
        keys = new byte[length][];
        values = new byte[length][];
        for(int i=0;i<length;i++){
            keys[i]=in.readByteArray();
            values[i]=in.readByteArray();
        }
    }
}

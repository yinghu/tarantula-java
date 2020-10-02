package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.service.ReplicationData;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class ReplicateOnDataScopeOperation extends Operation {



    private ReplicationData[] batch;

    public ReplicateOnDataScopeOperation() {
    }


    public ReplicateOnDataScopeOperation(String source,int partition, byte[] key, byte[] value) {
        this.batch = new ReplicationData[]{new ReplicationData(source,partition,key,value)};
    }
    public ReplicateOnDataScopeOperation(ReplicationData[] batch) {
        this.batch = batch;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.replicateAsBatch(batch);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(batch.length);
        for(ReplicationData d : batch){
            out.writeUTF(d.source);
            out.writeInt(d.partition);
            out.writeByteArray(d.key);
            out.writeByteArray(d.value);
        }
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        batch = new ReplicationData[in.readInt()];
        for(int i=0;i<batch.length;i++){
            batch[i]=new ReplicationData(in.readUTF(),in.readInt(),in.readByteArray(),in.readByteArray());
        }
    }
}

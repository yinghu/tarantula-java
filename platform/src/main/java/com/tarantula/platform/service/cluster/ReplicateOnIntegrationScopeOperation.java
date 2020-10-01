package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.service.ReplicationData;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class ReplicateOnIntegrationScopeOperation extends Operation {


    private ReplicationData[] batch;

    public ReplicateOnIntegrationScopeOperation() {
    }


    public ReplicateOnIntegrationScopeOperation( int partition,byte[] key, byte[] value) {
        this.batch = new ReplicationData[]{new ReplicationData(partition,key,value)};
    }
    public ReplicateOnIntegrationScopeOperation(ReplicationData[] batch) {
        this.batch = batch;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService cis = this.getService();
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
            batch[i]=new ReplicationData(in.readInt(),in.readByteArray(),in.readByteArray());
        }
    }
}

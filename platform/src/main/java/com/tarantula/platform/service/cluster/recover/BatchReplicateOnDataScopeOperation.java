package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.ReplicationData;

import java.io.IOException;

public class BatchReplicateOnDataScopeOperation extends Operation {

    private String source;

    private byte[] key;
    private byte[] value;

    private OnReplication[] onReplications;

    public BatchReplicateOnDataScopeOperation() {
    }


    public BatchReplicateOnDataScopeOperation(OnReplication[] onReplications) {
        this.onReplications = onReplications;
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.replicate(onReplications);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(onReplications.length);
        for(OnReplication onReplication : onReplications){
            out.writeUTF(onReplication.source());
            out.writeByteArray(onReplication.key());
            out.writeByteArray(onReplication.value());
        }
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        int sz = in.readInt();
        this.onReplications = new OnReplication[sz];
        for(int i=0;i<sz;i++){
            source = in.readUTF();
            key = in.readByteArray();
            value = in.readByteArray();
            this.onReplications[i]=new ReplicationData(source,key,value);
        }
    }
}

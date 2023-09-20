package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.ReplicationData;

import java.io.IOException;

public class BatchReplicateOnIntegrationScopeOperation extends Operation {

    private String nodeName;
    private OnReplication[] onReplications;
    private int size;

    public BatchReplicateOnIntegrationScopeOperation() {
    }


    public BatchReplicateOnIntegrationScopeOperation(String nodeName,OnReplication[] onReplications,int size) {
        this.nodeName = nodeName;
        this.onReplications = onReplications;
        this.size = size;
    }

    @Override
    public void run() throws Exception {
        AccessIndexClusterService cis = this.getService();
        cis.replicate(onReplications);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(nodeName);
        out.writeInt(size);
        for(int i=0;i<size;i++){
            OnReplication onReplication = onReplications[i];
            //out.writeInt(onReplication.partition());
            out.writeByteArray(onReplication.key());
            out.writeByteArray(onReplication.value());
        }
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        nodeName = in.readUTF();
        int sz = in.readInt();
        onReplications = new OnReplication[sz];
        for(int i=0;i<sz;i++){
            //int  partition = in.readInt();
            byte[] key = in.readByteArray();
            byte[] value = in.readByteArray();
            onReplications[i] = new ReplicationData(nodeName,key,value);
        }
    }
}

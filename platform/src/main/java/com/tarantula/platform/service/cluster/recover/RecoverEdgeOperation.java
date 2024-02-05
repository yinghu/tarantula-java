package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.tarantula.platform.service.cluster.ClusterBatch;

import java.io.IOException;

public class RecoverEdgeOperation extends Operation {

    private String source;

    private String label;
    private byte[] key;
    private ClusterBatch value;

    public RecoverEdgeOperation() {
    }


    public RecoverEdgeOperation(String source,String label, byte[] key) {
        this.source = source;
        this.label = label;
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        value = cis.loadEdgeValueSet(source,label,key);
    }

    @Override
    public Object getResponse() {
        return this.value.size()==0? null : this.value;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.source);
        out.writeUTF(label);
        out.writeByteArray(key);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.source = in.readUTF();
        this.label = in.readUTF();
        this.key = in.readByteArray();
    }
}

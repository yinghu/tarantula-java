package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class DeleteEdgeFromLabelOperation extends Operation {

    private String source;
    private String label;
    private byte[] key;

    public DeleteEdgeFromLabelOperation() {
    }


    public DeleteEdgeFromLabelOperation(String source, String label, byte[] key) {
        this.source = source;
        this.label = label;
        this.key = key;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.deleteEdge(source,label,key);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.source);
        out.writeUTF(this.label);
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

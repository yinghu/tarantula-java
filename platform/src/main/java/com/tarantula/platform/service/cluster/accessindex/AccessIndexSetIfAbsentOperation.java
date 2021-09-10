package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.AccessIndex;

import java.io.IOException;


public class AccessIndexSetIfAbsentOperation extends Operation implements PartitionAwareOperation {

    //private boolean result;
    private AccessIndex accessIndex;
    private String accessKey;
    private int referenceId;

    public AccessIndexSetIfAbsentOperation() {
    }

    public AccessIndexSetIfAbsentOperation(String accessKey,int referenceId) {
        this.accessKey = accessKey;
        this.referenceId = referenceId;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        this.accessIndex = ais.setIfAbsent(accessKey,referenceId);
    }

    @Override
    public Object getResponse() {
        return this.accessIndex;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(accessKey);
        out.writeInt(referenceId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.accessKey = in.readUTF();
        this.referenceId = in.readInt();
    }

}

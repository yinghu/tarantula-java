package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.AccessIndex;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class AccessIndexGetOperation extends Operation implements PartitionAwareOperation {


    private String accessKey;

    private AccessIndex accessIndex;

    public AccessIndexGetOperation() {
    }


    public AccessIndexGetOperation(String accessKey) {
        this.accessKey = accessKey;
    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        accessIndex = ais.get(accessKey);
    }

    @Override
    public Object getResponse() {
        return this.accessIndex;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.accessKey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.accessKey = in.readUTF();
    }
}

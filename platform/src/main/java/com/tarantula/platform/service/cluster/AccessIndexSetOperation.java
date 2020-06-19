package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.AccessIndex;

import java.io.IOException;

/**
 * updated by yinghu lu on 5/28/2019.
 */
public class AccessIndexSetOperation extends Operation implements PartitionAwareOperation {

    //private boolean result;
    private AccessIndex accessIndex;
    private String accessKey;

    public AccessIndexSetOperation() {
    }

    public AccessIndexSetOperation(String accessKey) {
        this.accessKey = accessKey;

    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        this.accessIndex = ais.set(accessKey);
    }

    @Override
    public Object getResponse() {
        return this.accessIndex;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(accessKey);

    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.accessKey = in.readUTF();
    }

}

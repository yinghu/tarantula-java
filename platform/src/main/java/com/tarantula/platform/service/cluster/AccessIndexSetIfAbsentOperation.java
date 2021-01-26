package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.AccessIndex;

import java.io.IOException;

/**
 * created by yinghu lu on 1/26/2021.
 */
public class AccessIndexSetIfAbsentOperation extends Operation implements PartitionAwareOperation {

    //private boolean result;
    private AccessIndex accessIndex;
    private String accessKey;

    public AccessIndexSetIfAbsentOperation() {
    }

    public AccessIndexSetIfAbsentOperation(String accessKey) {
        this.accessKey = accessKey;

    }
    @Override
    public void run() throws Exception {
        AccessIndexClusterService ais = this.getService();
        this.accessIndex = ais.setIfAbsent(accessKey);
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

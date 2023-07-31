package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.service.KeyIndex;
import com.tarantula.platform.service.cluster.accessindex.AccessIndexClusterService;

import java.io.IOException;


public class KeyIndexSetIfAbsentOperation extends Operation implements PartitionAwareOperation {

    private KeyIndex keyIndex;
    private String accessKey;

    public KeyIndexSetIfAbsentOperation() {
    }

    public KeyIndexSetIfAbsentOperation(String accessKey,KeyIndex pending) {
        this.accessKey = accessKey;
        this.keyIndex = pending;
    }
    @Override
    public void run() throws Exception {
        KeyIndexClusterService ais = this.getService();
        this.keyIndex = ais.setIfAbsent(accessKey,keyIndex);
    }

    @Override
    public Object getResponse() {
        return this.keyIndex;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(accessKey);
        out.writeObject(keyIndex);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.accessKey = in.readUTF();
        this.keyIndex = in.readObject();
    }

}

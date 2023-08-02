package com.tarantula.platform.service.cluster.keyindex;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.service.KeyIndex;

import java.io.IOException;


public class KeyIndexSetOperation extends Operation implements PartitionAwareOperation {

    private KeyIndex keyIndex;
    //private String accessKey;

    public KeyIndexSetOperation() {
    }

    public KeyIndexSetOperation(KeyIndex pending) {
        //this.accessKey = accessKey;
        this.keyIndex = pending;
    }
    @Override
    public void run() throws Exception {
        KeyIndexClusterService ais = this.getService();
        //this.keyIndex = ais.setIfAbsent(accessKey,keyIndex);
    }

    @Override
    public Object getResponse() {
        return this.keyIndex;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        //out.writeUTF(accessKey);
        out.writeObject(keyIndex);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        //this.accessKey = in.readUTF();
        this.keyIndex = in.readObject();
    }

}

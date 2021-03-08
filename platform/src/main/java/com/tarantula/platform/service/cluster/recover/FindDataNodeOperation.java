package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class FindDataNodeOperation extends Operation {

    private String source;
    private byte[] key;
    private String memberId;

    public FindDataNodeOperation() {
    }


    public FindDataNodeOperation(String source, byte[] key) {
        this.source = source;
        this.key = key;
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        memberId = cis.onDataNode(source,key);
    }

    @Override
    public Object getResponse() {
        return memberId;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(source);
        out.writeByteArray(key);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        source = in.readUTF();
        key = in.readByteArray();
    }
}

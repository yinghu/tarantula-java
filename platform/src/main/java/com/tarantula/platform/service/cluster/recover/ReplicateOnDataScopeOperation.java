package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.util.BufferUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ReplicateOnDataScopeOperation extends Operation {

    private String nodeName;
    private String source;
    private String label;
    private byte[] key;
    private byte[] value;


    public ReplicateOnDataScopeOperation() {
    }


    public ReplicateOnDataScopeOperation(String nodeName,String source,String label, byte[] key, byte[] value) {
        this.nodeName = nodeName;
        this.source = source;
        this.label = label;
        this.key = key;
        this.value = value;
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.replicate(nodeName,source,label,key,value);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(nodeName);
        out.writeUTF(source);
        out.writeUTF(label);
        out.writeByteArray(key);
        out.writeByteArray(value);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        nodeName = in.readUTF();
        source = in.readUTF();
        label = in.readUTF();
        key = in.readByteArray();
        value = in.readByteArray();
    }
}

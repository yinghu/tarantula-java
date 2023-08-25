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

    //private byte[] key;
    //private byte[] value;

    private ByteBuffer bkey = ByteBuffer.allocateDirect(100);
    private ByteBuffer vkey = ByteBuffer.allocateDirect(500);

    public ReplicateOnDataScopeOperation() {
    }


    public ReplicateOnDataScopeOperation(String nodeName,String source, byte[] key, byte[] value) {
        this.nodeName = nodeName;
        this.source = source;
        bkey.put(key).flip();
        //this.key = key;
        //this.value = value;
        vkey.put(value).flip();
    }



    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        cis.replicate(nodeName,source,BufferUtil.toArray(bkey),BufferUtil.toArray(vkey));
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
        out.writeByteArray(BufferUtil.toArray(bkey));
        out.writeByteArray(BufferUtil.toArray(vkey));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        nodeName = in.readUTF();
        source = in.readUTF();
        //key = in.readByteArray();
        //value = in.readByteArray();
        bkey.put(in.readByteArray());
        vkey.put(in.readByteArray());
        in.readInt();
    }
}

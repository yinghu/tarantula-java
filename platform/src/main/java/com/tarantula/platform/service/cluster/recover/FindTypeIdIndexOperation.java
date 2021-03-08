package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * updated by yinghu lu on 7/10/2020.
 */
public class FindTypeIdIndexOperation extends Operation {

    private String typeId;

    private byte[] value;

    public FindTypeIdIndexOperation() {
    }


    public FindTypeIdIndexOperation(String typeId) {
        this.typeId = typeId;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cis = this.getService();
        value = cis.loadTypeIdIndex(typeId);
    }

    @Override
    public Object getResponse() {
        return this.value;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.typeId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
    }
}

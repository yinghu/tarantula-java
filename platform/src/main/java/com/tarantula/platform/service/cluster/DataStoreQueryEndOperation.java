package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class DataStoreQueryEndOperation extends Operation {

    private String source;

    public DataStoreQueryEndOperation() {
    }


    public DataStoreQueryEndOperation(String source) {
        this.source = source;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        cds.queryEnd(source);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(source);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.source = in.readUTF();
    }
}

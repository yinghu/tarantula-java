package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class LoadModuleIndexOperation extends Operation {

    private byte[] index;

    public LoadModuleIndexOperation() {
    }

    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        index =  cds.loadModuleIndex();
    }

    @Override
    public Object getResponse() {
        return index;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
    }
}

package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 5/15/2020.
 */
public class ShutdownApplicationOperation extends Operation {

    private String typeId;
    private String applicationkey;
    public ShutdownApplicationOperation() {
    }


    public ShutdownApplicationOperation(String typeId, String applicationkey) {
        this.typeId = typeId;
        this.applicationkey = applicationkey;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.shutdownApplication(typeId,applicationkey);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeUTF(applicationkey);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.applicationkey = in.readUTF();
    }
}

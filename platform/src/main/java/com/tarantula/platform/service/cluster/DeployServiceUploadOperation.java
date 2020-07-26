package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 *  created by yinghu lu on 7/26/2020.
 */
public class DeployServiceUploadOperation extends Operation {


    private String fileName;

    private byte[] payload;

    public DeployServiceUploadOperation() {
    }


    public DeployServiceUploadOperation(String fileName,byte[] payload) {
        this.fileName = fileName;
        this.payload = payload;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.upload(fileName,payload);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(fileName);
        out.writeByteArray(payload);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.fileName = in.readUTF();
        this.payload = in.readByteArray();
    }
}

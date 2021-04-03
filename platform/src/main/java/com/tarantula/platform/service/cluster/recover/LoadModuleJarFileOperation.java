package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class LoadModuleJarFileOperation extends Operation {

    private byte[] data;
    private String fileName;
    public LoadModuleJarFileOperation() {
    }
    public LoadModuleJarFileOperation(String fileName) {
        this.fileName = fileName;
    }
    @Override
    public void run() throws Exception {
        ClusterRecoverService cds = this.getService();
        data = cds.loadModuleJarFile(fileName);
    }

    @Override
    public Object getResponse() {
        return data;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(fileName);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.fileName = in.readUTF();
    }
}

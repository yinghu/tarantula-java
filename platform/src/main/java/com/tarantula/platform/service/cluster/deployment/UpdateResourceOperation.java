package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class UpdateResourceOperation extends Operation {

    private String contentUrl;
    private String resourceName;
    public UpdateResourceOperation() {
    }


    public UpdateResourceOperation(String contentUrl, String resourceName) {
        this.contentUrl = contentUrl;
        this.resourceName = resourceName;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.onUpdateResource(contentUrl,resourceName);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(contentUrl);
        out.writeUTF(resourceName);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.contentUrl = in.readUTF();
        this.resourceName = in.readUTF();
    }
}

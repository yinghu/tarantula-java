package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

/**
 * created by yinghu lu on 2/8/2021.
 */
public class DeployModuleOperation extends Operation {

    private String contentUrl;
    private String resourceName;
    public DeployModuleOperation() {
    }


    public DeployModuleOperation(String contentUrl, String resourceName) {
        this.contentUrl = contentUrl;
        this.resourceName = resourceName;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.deployModule(contentUrl,resourceName);
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

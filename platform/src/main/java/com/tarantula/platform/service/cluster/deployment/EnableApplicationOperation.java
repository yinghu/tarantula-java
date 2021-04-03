package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class EnableApplicationOperation extends Operation {


    private String  applicationId;
    private boolean enabled;

    private String result;

    public EnableApplicationOperation() {
    }


    public EnableApplicationOperation(String applicationId, boolean enabled) {
        this.applicationId = applicationId;
        this.enabled = enabled;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = enabled?cds.enableApplication(applicationId):cds.disableApplication(applicationId);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(applicationId);
        out.writeBoolean(enabled);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.applicationId = in.readUTF();
        this.enabled = in.readBoolean();
    }
}

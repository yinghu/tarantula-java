package com.tarantula.platform.service.cluster.item;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ConfigurationReleasedOperation extends Operation {

    private String gameServiceName;
    private String serviceName;
    private int publishId;
    private boolean ret;

    public ConfigurationReleasedOperation() {
    }


    public ConfigurationReleasedOperation(String gameServiceName, String serviceName,int publishId) {
        this.gameServiceName = gameServiceName;
        this.serviceName = serviceName;
        this.publishId = publishId;
    }

    @Override
    public void run() throws Exception {
        ItemClusterService cis = this.getService();
        this.ret = cis.onRelease(gameServiceName,serviceName,publishId);
    }

    @Override
    public Object getResponse() {
        return ret;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(gameServiceName);
        out.writeUTF(serviceName);
        out.writeInt(publishId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        gameServiceName = in.readUTF();
        serviceName = in.readUTF();
        publishId = in.readInt();
    }
}

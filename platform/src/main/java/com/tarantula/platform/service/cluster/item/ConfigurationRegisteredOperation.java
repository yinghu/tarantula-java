package com.tarantula.platform.service.cluster.item;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class ConfigurationRegisteredOperation extends Operation {

    private String gameServiceName;
    private String serviceName;
    private int publishId;
    private int configurationId;
    private boolean ret;

    public ConfigurationRegisteredOperation() {
    }


    public ConfigurationRegisteredOperation(String gameServiceName, String serviceName, int publishId,int configurationId) {
        this.gameServiceName = gameServiceName;
        this.serviceName = serviceName;
        this.publishId = publishId;
        this.configurationId = configurationId;
    }

    @Override
    public void run() throws Exception {
        ItemClusterService cis = this.getService();
        this.ret = cis.onRegister(gameServiceName,serviceName,publishId,configurationId);
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
        out.writeInt(configurationId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        gameServiceName = in.readUTF();
        serviceName = in.readUTF();
        publishId = in.readInt();
        configurationId = in.readInt();
    }
}

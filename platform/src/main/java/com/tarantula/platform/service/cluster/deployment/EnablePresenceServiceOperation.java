package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;

import java.io.IOException;

public class EnablePresenceServiceOperation extends Operation {

    private String root;
    private String password;
    private String clusterNameSuffix;
    private String host;

    public EnablePresenceServiceOperation() {
    }

    public EnablePresenceServiceOperation(String root,String password,String clusterNameSuffix,String host) {
        this.root = root;
        this.password = password;
        this.clusterNameSuffix = clusterNameSuffix;
        this.host = host;
    }

    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.enablePresenceService(root,password,clusterNameSuffix,host);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(root);
        out.writeUTF(password);
        out.writeUTF(clusterNameSuffix);
        out.writeUTF(host);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        root = in.readUTF();
        password = in.readUTF();
        clusterNameSuffix = in.readUTF();
        host = in.readUTF();
    }
}

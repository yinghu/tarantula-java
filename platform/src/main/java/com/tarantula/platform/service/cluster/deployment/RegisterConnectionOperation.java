package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Connection;

import java.io.IOException;

public class RegisterConnectionOperation extends Operation {


    private String  typeId;
    private Connection connection;


    public RegisterConnectionOperation() {
    }


    public RegisterConnectionOperation(String typeId, Connection connection) {
        this.typeId = typeId;
        this.connection = connection;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        cds.registerConnection(typeId,connection);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeObject(connection);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.connection = in.readObject();
    }
}

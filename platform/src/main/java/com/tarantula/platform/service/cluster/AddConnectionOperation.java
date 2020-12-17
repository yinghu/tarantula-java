package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Connection;

import java.io.IOException;

/**
 * created by yinghu lu on 12/16/2020.
 */
public class AddConnectionOperation extends Operation implements PartitionAwareOperation {

    //private boolean result;
    private Connection connection;
    private String typeId;

    public AddConnectionOperation() {
    }

    public AddConnectionOperation(String typeId,Connection connection) {
        this.typeId = typeId;
        this.connection = connection;

    }
    @Override
    public void run() throws Exception {
        ClusterDeployService ais = this.getService();
        ais.addConnection(typeId,connection);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);

    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
    }

}

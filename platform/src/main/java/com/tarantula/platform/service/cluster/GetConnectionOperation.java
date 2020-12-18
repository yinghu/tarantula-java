package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

/**
 * created by yinghu lu on 12/16/2020.
 */
public class GetConnectionOperation extends Operation implements PartitionAwareOperation {

    //private boolean result;
    private byte[] connection;
    private String typeId;
    private byte[] payload;
    public GetConnectionOperation() {
    }

    public GetConnectionOperation(String typeId,byte[] payload) {
        this.typeId = typeId;
        this.payload = payload;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService ais = this.getService();
        connection = ais.getConnection(typeId,payload);
    }

    @Override
    public Object getResponse() {
        return connection;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeByteArray(payload);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.payload = in.readByteArray();
    }

}

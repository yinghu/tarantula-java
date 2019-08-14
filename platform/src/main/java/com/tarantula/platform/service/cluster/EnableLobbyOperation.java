package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import java.io.IOException;

/**
 * updated by yinghu lu on 5/29/2019.
 */
public class EnableLobbyOperation extends Operation {


    private String  typeId;
    private boolean enabled;

    private String result;

    public EnableLobbyOperation() {
    }


    public EnableLobbyOperation(String typeId,boolean enabled) {
        this.typeId = typeId;
        this.enabled = enabled;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        this.result = cds.enableLobby(typeId,enabled);
    }

    @Override
    public Object getResponse() {
        return this.result;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeBoolean(enabled);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.enabled = in.readBoolean();
    }
}

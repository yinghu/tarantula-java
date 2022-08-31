package com.tarantula.platform.service.cluster.deployment;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.Channel;

import java.io.IOException;

public class RegisterChannelOperation extends Operation implements PartitionAwareOperation {


    private String  typeId;
    private Channel channel;
    private boolean suc;

    public RegisterChannelOperation() {
    }


    public RegisterChannelOperation(String typeId, Channel channel) {
        this.typeId = typeId;
        this.channel = channel;
    }
    @Override
    public void run() throws Exception {
        ClusterDeployService cds = this.getService();
        suc = cds.onRegisterChannel(typeId,channel);
    }

    @Override
    public Object getResponse() {
        return suc;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(typeId);
        out.writeObject(channel);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        this.typeId = in.readUTF();
        this.channel = in.readObject();
    }
}

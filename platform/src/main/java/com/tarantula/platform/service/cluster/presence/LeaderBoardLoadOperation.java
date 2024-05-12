package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class LeaderBoardLoadOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String category;
    private String classifier;
    private byte[] response;
    public LeaderBoardLoadOperation(){}
    public LeaderBoardLoadOperation(String serviceName,String category,String classifier){
        this.serviceName = serviceName;
        this.category = category;
        this.classifier = classifier;
    }

    @Override
    public void run() throws Exception {
        PresenceClusterService ais = this.getService();
        response = ais.onLoadLeaderBoard(serviceName,category,classifier);
    }

    @Override
    public Object getResponse() {
        return response;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(category);
        out.writeUTF(classifier);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        category = in.readUTF();
        classifier = in.readUTF();
    }
}

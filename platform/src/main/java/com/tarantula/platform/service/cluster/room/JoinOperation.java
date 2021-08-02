package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.game.Rating;
import com.tarantula.game.Room;

import java.io.IOException;

public class JoinOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private Rating rating;
    private Room stub;
    public JoinOperation(){}
    public JoinOperation(String serviceName,Rating rating){
        this.serviceName = serviceName;
        this.rating = rating;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        stub = ais.join(serviceName,rating);
    }

    @Override
    public Object getResponse() {
        return stub;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeObject(rating);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        rating = in.readObject();
    }
}

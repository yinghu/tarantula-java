package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class RoomLoadOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String roomId;

    public RoomLoadOperation(){}
    public RoomLoadOperation(String serviceName, String roomId){
        this.serviceName = serviceName;
        this.roomId = roomId;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        ais.load(serviceName,roomId);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(roomId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        roomId = in.readUTF();
    }
}

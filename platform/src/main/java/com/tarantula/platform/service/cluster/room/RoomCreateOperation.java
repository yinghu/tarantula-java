package com.tarantula.platform.service.cluster.room;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;

public class RoomCreateOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String roomId;

    public RoomCreateOperation(){}
    public RoomCreateOperation(String serviceName, String roomId){
        this.serviceName = serviceName;
        this.roomId = roomId;
    }

    @Override
    public void run() throws Exception {
        RoomClusterService ais = this.getService();
        ais.create(serviceName,roomId);
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

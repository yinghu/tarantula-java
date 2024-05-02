package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.platform.service.cluster.room.RoomClusterService;

import java.io.IOException;

public class ProfileNameSequenceOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;


    private String profileName;

    private int sequence;
    public ProfileNameSequenceOperation(){}

    public ProfileNameSequenceOperation(String serviceName, String profileName){
        this.serviceName = serviceName;
        this.profileName = profileName;
    }

    @Override
    public void run() throws Exception {
        PresenceClusterService ais = this.getService();
        sequence = ais.onProfileSequence(serviceName,profileName);
    }

    @Override
    public Object getResponse() {
        return sequence;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(profileName);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        profileName = in.readUTF();
    }
}

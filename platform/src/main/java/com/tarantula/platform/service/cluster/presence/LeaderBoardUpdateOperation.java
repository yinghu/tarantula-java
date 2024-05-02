package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;
import com.icodesoftware.LeaderBoard;
import com.tarantula.platform.presence.leaderboard.LeaderBoardEntry;

import java.io.IOException;

public class LeaderBoardUpdateOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private LeaderBoard.Entry entry;
    public LeaderBoardUpdateOperation(){}
    public LeaderBoardUpdateOperation(String serviceName, LeaderBoard.Entry leaderBoardEntry){
        this.serviceName = serviceName;
        this.entry = leaderBoardEntry;
    }

    @Override
    public void run() throws Exception {
        PresenceClusterService ais = this.getService();
        ais.onUpdateLeaderBoard(serviceName,entry);
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(serviceName);
        out.writeUTF(entry.category());
        out.writeUTF(entry.classifier());
        out.writeLong(entry.systemId());
        out.writeDouble(entry.value());
        out.writeLong(entry.timestamp());
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        entry = LeaderBoardEntry.from(in.readUTF(),in.readUTF(),in.readLong(),in.readDouble(),in.readLong());
    }
}

package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.PartitionAwareOperation;

import java.io.IOException;


public class TournamentTryScheduleOperation extends Operation implements PartitionAwareOperation {

    private String serviceName;
    private String scheduleId;
    private boolean ret;

    public TournamentTryScheduleOperation() {
    }


    public TournamentTryScheduleOperation(String serviceName, String scheduleId) {
        this.serviceName = serviceName;
        this.scheduleId = scheduleId;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        ret = ais.trySchedule(serviceName,scheduleId);
    }

    @Override
    public Object getResponse() {
        return this.ret;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.scheduleId);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        scheduleId = in.readUTF();
    }
}

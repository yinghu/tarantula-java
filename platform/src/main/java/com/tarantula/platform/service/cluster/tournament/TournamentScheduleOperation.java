package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.tournament.DefaultTournamentSchedule;

import java.io.IOException;
import java.time.LocalDateTime;


public class TournamentScheduleOperation extends Operation{

    private String serviceName;
    private Tournament.Schedule schedule;
    private Tournament tournament;
    public TournamentScheduleOperation() {
    }


    public TournamentScheduleOperation(String serviceName, Tournament.Schedule schedule) {
        this.serviceName = serviceName;
        this.schedule = schedule;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        tournament = ais.schedule(serviceName,this.schedule);
    }

    @Override
    public Object getResponse() {
        return tournament;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.schedule.type());
        out.writeUTF(this.schedule.name());
        out.writeUTF(this.schedule.schedule());
        out.writeInt(this.schedule.maxEntriesPerInstance());
        out.writeInt(this.schedule.instanceDurationInMinutes());
        out.writeLong(TimeUtil.toUTCMilliseconds(this.schedule.startTime()));
        out.writeLong(TimeUtil.toUTCMilliseconds(this.schedule.closeTime()));
        out.writeLong(TimeUtil.toUTCMilliseconds(this.schedule.endTime()));
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        serviceName = in.readUTF();
        String type = in.readUTF();
        String name = in.readUTF();
        String schedule = in.readUTF();
        int mz = in.readInt();
        int dur = in.readInt();
        LocalDateTime start = TimeUtil.fromUTCMilliseconds(in.readLong());
        LocalDateTime close = TimeUtil.fromUTCMilliseconds(in.readLong());
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(in.readLong());
        this.schedule = new DefaultTournamentSchedule(type,name,schedule,start,close,end,dur,mz);
    }
}

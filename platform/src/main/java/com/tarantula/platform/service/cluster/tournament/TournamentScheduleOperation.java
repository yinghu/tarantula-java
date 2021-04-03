package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.Operation;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.cluster.tournament.TournamentClusterService;
import com.tarantula.platform.tournament.DefaultTournamentSchedule;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;


public class TournamentScheduleOperation extends Operation{

    private String serviceName;
    private Tournament.Schedule schedule;
    private byte[] data;
    public TournamentScheduleOperation() {
    }


    public TournamentScheduleOperation(String serviceName, Tournament.Schedule schedule) {
        this.serviceName = serviceName;
        this.schedule = schedule;
    }
    @Override
    public void run() throws Exception {
        TournamentClusterService ais = this.getService();
        Tournament tournament = ais.schedule(serviceName,this.schedule);
        Map<String,Object> _map = tournament.toMap();
        _map.put("tournamentId",tournament.distributionKey());
        this.data = JsonUtil.toJson(_map);
    }

    @Override
    public Object getResponse() {
        return data;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeUTF(this.serviceName);
        out.writeUTF(this.schedule.type());
        out.writeUTF(this.schedule.description());
        out.writeUTF(this.schedule.icon());
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
        String desc = in.readUTF();
        String icon = in.readUTF();
        int mz = in.readInt();
        int dur = in.readInt();
        LocalDateTime start = TimeUtil.fromUTCMilliseconds(in.readLong());
        LocalDateTime close = TimeUtil.fromUTCMilliseconds(in.readLong());
        LocalDateTime end = TimeUtil.fromUTCMilliseconds(in.readLong());
        this.schedule = new DefaultTournamentSchedule(type,desc,icon,start,close,end,dur,mz);
    }
}

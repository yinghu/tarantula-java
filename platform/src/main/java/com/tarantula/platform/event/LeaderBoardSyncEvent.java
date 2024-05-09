package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Event;
import com.icodesoftware.LeaderBoard;
import com.tarantula.platform.presence.leaderboard.LeaderBoardEntry;


import java.io.IOException;

public class LeaderBoardSyncEvent extends Data implements Event {

    public LeaderBoard.Entry entry;

    public LeaderBoardSyncEvent(){

    }
    public LeaderBoardSyncEvent(String destination,LeaderBoard.Entry entry){
        this.destination = destination;
        this.entry = entry;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",entry.category());
        out.writeUTF("3",entry.classifier());
        out.writeLong("4",entry.systemId());
        out.writeDouble("5",entry.value());
        out.writeLong("6",entry.timestamp());
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.entry = LeaderBoardEntry.from(in.readUTF("2"),in.readUTF("3"),in.readLong("4"),in.readDouble("5"),in.readLong("6"));
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.LEADER_BOARD_SYNC_EVENT_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public String toString(){
        return "leader board sync event";
    }
}

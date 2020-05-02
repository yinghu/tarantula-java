package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.LeaderBoard;

import java.io.IOException;

public class LeaderBoardGlobalEvent extends Data implements Event {


    public LeaderBoardGlobalEvent(){

    }
    public LeaderBoardGlobalEvent(String destination,String trackId, LeaderBoard.Entry entry){
        this.destination = destination;
        this.trackId = trackId;

        this.name = entry.category();
        this.index = entry.classifier();
        this.version = entry.version();
        this.owner = entry.owner();
        this.balance = entry.value();
        this.timestamp = entry.timestamp();
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("2",this.trackId);
        out.writeUTF("3",this.name);//category
        out.writeUTF("4",this.index);//classifier
        out.writeInt("5",this.version);
        out.writeUTF("6",this.owner);
        out.writeDouble("7",this.balance);
        out.writeLong("8",this.timestamp);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.trackId = in.readUTF("2");
        this.name = in.readUTF("3");
        this.index = in.readUTF("4");
        this.version = in.readInt("5");
        this.owner = in.readUTF("6");
        this.balance = in.readDouble("7");
        this.timestamp = in.readLong("8");
    }
    @Override
    public int getFactoryId(){
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId(){
        return PortableEventRegistry.LEADER_BOARD_GLOBAL_EVENT_CID;
    }

}

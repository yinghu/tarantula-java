package com.tarantula.platform.event;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.Event;
import com.tarantula.LeaderBoard;

import java.io.IOException;

public class LeaderBoardGlobalEvent extends Data implements Event {


    public LeaderBoardGlobalEvent(){

    }
    public LeaderBoardGlobalEvent(String destination, LeaderBoard.Entry entry){
        this.destination = destination;
        this.instanceId = entry.category();
        this.clientId = entry.classifier();
        this.systemId = entry.owner();
        this.balance = entry.value();
        this.timestamp = entry.timestamp();
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.destination);
        out.writeUTF("3",this.instanceId);
        out.writeUTF("4",this.clientId);
        out.writeUTF("5",this.systemId);
        out.writeDouble("6",this.balance);
        out.writeLong("7",this.timestamp);
    }

    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.destination = in.readUTF("1");
        this.instanceId = in.readUTF("3");
        this.clientId = in.readUTF("4");
        this.systemId = in.readUTF("5");
        this.balance = in.readDouble("6");
        this.timestamp = in.readLong("7");
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

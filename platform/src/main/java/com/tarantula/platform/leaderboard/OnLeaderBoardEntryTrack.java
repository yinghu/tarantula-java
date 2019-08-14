package com.tarantula.platform.leaderboard;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnLeaderBoard;
import com.tarantula.platform.RecoverableObject;

import java.io.IOException;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class OnLeaderBoardEntryTrack extends RecoverableObject implements OnLeaderBoard.Entry{
    private String systemId;
    private String header;
    private String category;


    public OnLeaderBoardEntryTrack(){
    }
    public OnLeaderBoardEntryTrack(String category){
        this();
        this.category = category;
    }
    public String category(){
        return this.category;
    }
    public void category(String category){
        this.category = category;
    }

    public String systemId() {
        return this.systemId;
    }

    public void systemId(String systemId) {
        this.systemId = systemId;
    }

    public String header(){
        return this.header;
    }
    public void header(String header){
        this.header = header;
    }
    public  void value(String classifier,double value){
        this.properties.put(classifier,value);
    }
    public double value(String classifier){
        return (double)properties.computeIfAbsent(classifier,(String k)-> 0d);
    }
    @Override
    public String toString(){
        return "Leader Board Entry["+systemId+"/"+category+"]";
    }


    public int getFactoryId() {
        return LeaderBoardPortableRegistry.OID;
    }


    public int getClassId() {
        return LeaderBoardPortableRegistry.ON_LEADER_BOARD_ENTRY_CID;
    }



    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.category);
        out.writeDouble("2", this.value(OnLeaderBoard.TOTAL));
        out.writeDouble("3",this.value(OnLeaderBoard.DAILY));
        out.writeDouble("4",this.value(OnLeaderBoard.WEEKLY));
        out.writeDouble("5",this.value(OnLeaderBoard.MONTHLY));
        out.writeDouble("6",this.value(OnLeaderBoard.YEARLY));
    }


    public void readPortable(PortableReader in) throws IOException {
        this.category = in.readUTF("1");
        this.value(OnLeaderBoard.TOTAL,in.readDouble("2"));
        this.value(OnLeaderBoard.DAILY,in.readDouble("3"));
        this.value(OnLeaderBoard.WEEKLY,in.readDouble("4"));
        this.value(OnLeaderBoard.MONTHLY,in.readDouble("5"));
        this.value(OnLeaderBoard.YEARLY,in.readDouble("6"));
    }
}

package com.tarantula.platform.leaderboard;


import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnLeaderBoard;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;

/**
 * Updated by yinghu on 6/15/2018.
 */
public class OnLeaderBoardTrack extends OnApplicationHeader implements OnLeaderBoard {

    private String leaderBoardHeader;

    private OnLeaderBoard.Entry[] entryList;

    public OnLeaderBoardTrack(){}

    public OnLeaderBoardTrack(String leaderBoardHeader,OnLeaderBoard.Entry[] clist){
        this.leaderBoardHeader = leaderBoardHeader;
        this.entryList = clist;
    }

    public String leaderBoardHeader() {
        return this.leaderBoardHeader;
    }


    public OnLeaderBoard.Entry[] entryList() {
        return this.entryList;
    }

    @Override
    public int getFactoryId() {
        return LeaderBoardPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return LeaderBoardPortableRegistry.ON_LEADER_BOARD_CID;
    }


    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("1",this.systemId);
        out.writeUTF("3",this.leaderBoardHeader);
        //out.writePortableArray("4",this.entryList);
    }

    public void readPortable(PortableReader in) throws IOException {
        this.systemId = in.readUTF("1");
        this.leaderBoardHeader = in.readUTF("3");
        Portable[] plist = in.readPortableArray("4");
        this.entryList = new OnLeaderBoard.Entry[plist.length];
        for(int i=0;i<plist.length;i++){
            this.entryList[i]=(OnLeaderBoard.Entry)plist[i];
        }
    }

}

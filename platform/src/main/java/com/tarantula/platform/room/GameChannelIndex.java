package com.tarantula.platform.room;

public class GameChannelIndex {

    public ChannelStub channelStub;
    public int partitionId;
    public boolean localManaged;

    public String serverId;

    public GameChannelIndex(int partitionId, boolean localManaged){
        this.partitionId = partitionId;
        this.localManaged = localManaged;
    }

    @Override
    public String toString(){
        return serverId+"_"+channelStub.channelId();
    }
}

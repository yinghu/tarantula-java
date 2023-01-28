package com.tarantula.platform.room;


import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.platform.event.PortableEventRegistry;

import java.util.Map;

//one time room join ticket
public class ChannelStub extends GameChannel{

    public String serverId;
    public String roomId;

    public ChannelStub(){

    }

    public ChannelStub(int channelId,int sessionId){
        this.channelId = channelId;
        this.sessionId = sessionId;
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.CHANNEL_STUB_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",channelId);
        this.properties.put("2",sessionId);
        this.properties.put("3",roomId);
        this.properties.put("4",serverId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.channelId = ((Number)properties.get("1")).intValue();
        this.sessionId = ((Number)properties.get("2")).intValue();
        this.roomId = (String) properties.get("3");
        this.serverId = (String) properties.get("4");
    }


    @Override
    public String toString(){
        return "ChannelId ["+channelId+"] SessionId ["+sessionId+"] RoomId ["+roomId+"]";
    }

    @Override
    public int hashCode(){
        return Integer.hashCode(channelId);
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof ChannelStub)) return false;
        return channelId == ((ChannelStub)obj).channelId;
    }

    public void sessionId(int sessionId){
        this.sessionId = sessionId;
    }
    public Channel toChannel(Connection connection,byte[] key,int timeout){
        return new GameChannel(channelId,sessionId,connection,key,timeout);
    }

    public void reset(){

    }

}
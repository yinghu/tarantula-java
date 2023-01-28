package com.tarantula.platform.room;

public class RoomStatus {

    public final static int JOINED = 0;
    public final static int LEFT = 1;

    public final static int FULL = 2;
    public final static int EMPTY = 3;

    public final String roomId;
    public final String zoneId;
    public final int status;

    public RoomStatus(String zoneId , String  roomId,int status){
        this.zoneId = zoneId;
        this.roomId = roomId;
        this.status = status;
    }

    public String toString(){
        return "ZoneId ["+zoneId+"] RoomId ["+roomId+"] Status ["+status+"]";
    }
}

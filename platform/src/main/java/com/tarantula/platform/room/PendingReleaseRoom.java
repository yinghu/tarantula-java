package com.tarantula.platform.room;

import java.time.LocalDateTime;

public class PendingReleaseRoom {

    public String roomId;
    public LocalDateTime pendingSchedule;

    public PendingReleaseRoom(String roomId,LocalDateTime pendingSchedule){
        this.roomId = roomId;
        this.pendingSchedule = pendingSchedule;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this) return true;
        return roomId.equals(((PendingReleaseRoom)obj).roomId);
    }
    @Override
    public int hashCode(){
        return roomId.hashCode();
    }

}

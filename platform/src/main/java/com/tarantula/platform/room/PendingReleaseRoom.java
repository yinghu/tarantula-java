package com.tarantula.platform.room;

import com.icodesoftware.Room;

import java.time.LocalDateTime;

public class PendingReleaseRoom {

    public Room room;
    public LocalDateTime pendingSchedule;

    public PendingReleaseRoom(Room room,LocalDateTime pendingSchedule){
        this.room = room;
        this.pendingSchedule = pendingSchedule;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==this) return true;
        return room.distributionKey().equals(((PendingReleaseRoom)obj).room.distributionKey());
    }
    @Override
    public int hashCode(){
        return room.distributionKey().hashCode();
    }

}

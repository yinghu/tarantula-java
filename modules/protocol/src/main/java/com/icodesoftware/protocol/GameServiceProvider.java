package com.icodesoftware.protocol;

import com.icodesoftware.Room;

public interface GameServiceProvider {

    void updateStatistics(Room room, String system,long stub, String name, double delta);
    void updateExperience(Room room,String system,long stub,double delta);
}

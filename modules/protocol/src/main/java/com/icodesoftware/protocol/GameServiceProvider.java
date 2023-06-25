package com.icodesoftware.protocol;

import com.icodesoftware.Room;

public interface GameServiceProvider {

    void updateStatistics(Room room, String system,int stub, String name, double delta);
    void updateExperience(Room room,String system,int stub,double delta);
}

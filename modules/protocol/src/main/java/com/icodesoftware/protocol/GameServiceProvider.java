package com.icodesoftware.protocol;

import com.icodesoftware.Room;
import com.icodesoftware.Session;

public interface GameServiceProvider {
    void setup(GameContext gameContext);
    default void updateStatistics(Room room, String system,long stub, String name, double delta){}
    default void updateExperience(Room room,String system,long stub,double delta){}

    void startGame(Session session,byte[] payload) throws Exception;
    void updateGame(Session session,byte[] payload) throws Exception;
    void endGame(Session session,byte[] payload) throws Exception;
}

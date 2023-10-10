package com.icodesoftware.protocol;

import com.icodesoftware.Inventory;
import com.icodesoftware.OnAccess;
import com.icodesoftware.Session;

public interface GameServiceProvider extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener,Inventory.Listener{
    void setup(GameContext gameContext);

    void onJoined(Session session);
    void startGame(Session session,byte[] payload) throws Exception;
    void updateGame(Session session,byte[] payload) throws Exception;
    void endGame(Session session,byte[] payload) throws Exception;
    <T extends OnAccess> void onGameEvent(T event);

    void onLeft(Session session);
}

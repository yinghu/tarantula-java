package com.icodesoftware.protocol;

import com.icodesoftware.Inventory;
import com.icodesoftware.OnAccess;
import com.icodesoftware.Session;

public interface GameServiceProvider extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener{
    void setup(GameContext gameContext);

    void onJoined(Session session);
    void startGame(Session session,byte[] payload) throws Exception;
    void updateGame(Session session,byte[] payload) throws Exception;
    void endGame(Session session,byte[] payload) throws Exception;
    <T extends OnAccess> void onGameEvent(T event);

    void onInventory(Inventory inventory, Inventory.Stock stock);
    void onLeft(Session session);
}

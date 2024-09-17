package com.icodesoftware.protocol;

import com.icodesoftware.*;

import java.util.List;

public interface GameServiceProvider extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener,Inventory.Listener, Tournament.Listener,ChannelListener,ApplicationResource.Listener {
    void setup(GameContext gameContext);

    void onJoined(Session session,Room room);
    void startGame(Session session,byte[] payload) throws Exception;
    void updateGame(Session session,byte[] payload) throws Exception;
    void endGame(Session session,byte[] payload) throws Exception;
    <T extends OnAccess> void onGameEvent(T event);

    void onLeft(Session session);

    List<OnInbox> inbox(Session session);
}

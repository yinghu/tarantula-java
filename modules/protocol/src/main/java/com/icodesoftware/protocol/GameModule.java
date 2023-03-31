package com.icodesoftware.protocol;

import com.icodesoftware.ChannelListener;
import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;

public interface GameModule extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener, ChannelListener {

    void setup(Room room, GameContext gameContext);

    Room room();

    void registerRoomListener(RoomListener roomListener);
}

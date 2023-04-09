package com.icodesoftware.protocol;

import com.icodesoftware.Closable;
import com.icodesoftware.Resettable;
import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;
import com.icodesoftware.service.ServiceContext;

public interface GameModule extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener, ChannelListener, Closable, Resettable {

    void setup(Room room, GameContext gameContext);

    Room room();

    void registerRoomListener(RoomListener roomListener);

    default void update(byte[] payload){}

}

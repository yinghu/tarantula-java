package com.icodesoftware.protocol;

import com.icodesoftware.ChannelListener;
import com.icodesoftware.Room;

public interface GameModule extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener, ChannelListener {

    void setup(Room room, GameContext gameContext);

    Room room();
}

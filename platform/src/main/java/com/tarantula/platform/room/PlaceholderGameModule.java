package com.tarantula.platform.room;

import com.icodesoftware.Channel;
import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.GameContext;
import com.icodesoftware.protocol.GameModule;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;

public class PlaceholderGameModule implements GameModule {
    @Override
    public void onValidated(Channel channel) {

    }

    @Override
    public void onJoined(Channel channel) {

    }

    @Override
    public void onLeft(Channel channel) {

    }

    @Override
    public void setup(Room room, GameContext gameContext) {

    }

    @Override
    public Room room() {
        return null;
    }

    @Override
    public void registerRoomListener(RoomListener roomListener) {

    }

    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return new byte[0];
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {

    }
}

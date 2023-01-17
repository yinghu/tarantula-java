package com.icodesoftware.protocol;

import com.icodesoftware.Channel;
import com.icodesoftware.Connection;

public interface GameServerListener {

    String typeId();
    void onConnection(Connection connection);
    void onDisConnection(Connection connection);
    void onChannel(Channel channel);

    //distributed callbacks
    void onConnectionReleased(Connection connection);
    void onConnectionRegistered(Connection connection);
    void onConnectionVerified(String serverId);

}

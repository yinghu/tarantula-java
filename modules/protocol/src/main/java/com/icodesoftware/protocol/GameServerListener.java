package com.icodesoftware.protocol;

import com.icodesoftware.Channel;
import com.icodesoftware.Connection;

public interface GameServerListener {

    String typeId();
    boolean onConnection(Connection connection);
    void onStartConnection(Connection connection);
    void onDisConnection(Connection connection);
    boolean onChannel(Channel channel);

    //distributed callbacks
    void onConnectionReleased(Connection connection);
    void onConnectionStarted(Connection connection);
    void onConnectionRegistered(Connection connection);
    void onConnectionVerified(String serverId);

}

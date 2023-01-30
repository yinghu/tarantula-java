package com.icodesoftware.protocol;

import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.EndPoint;

public interface GameServerListener extends EndPoint.Listener {

    String typeId();
    OnAccess onConnection(Connection connection);
    void onStartConnection(Connection connection);
    void onDisConnection(Connection connection);
    boolean onChannel(Channel channel);

    //distributed callbacks
    void onConnectionReleased(Connection connection);
    void onConnectionStarted(Connection connection);
    void onConnectionRegistered(Connection connection);
    void onConnectionVerified(String serverId);

}

package com.icodesoftware.protocol;

import com.icodesoftware.Channel;
import com.icodesoftware.Connection;

public interface GameServerListener {

    String typeId();
    void onConnection(Connection connection);
    void onPing(String serverId);
    void onDisConnection(Connection connection);
    void onChannel(Channel channel);
}

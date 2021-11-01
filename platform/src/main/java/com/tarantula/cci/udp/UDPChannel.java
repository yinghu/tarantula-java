package com.tarantula.cci.udp;

import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.tarantula.platform.UniverseConnection;

public class UDPChannel implements Channel {

    @Override
    public int channelId() {
        return 1;
    }

    @Override
    public int sessionId() {
        return 1;
    }

    @Override
    public void write(byte[] bytes) {

    }

    public Connection connection(){
        Connection connection = new UniverseConnection();
        connection.serverId("serverId");
        connection.type("udp");
        connection.secured(true);
        connection.channelId(1);
        connection.host("10.0.0.192");
        connection.port(11933);
        return connection;
    }
}

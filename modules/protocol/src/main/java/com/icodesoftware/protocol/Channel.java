package com.icodesoftware.protocol;

import com.icodesoftware.Configurable;
import com.icodesoftware.Connection;
import com.icodesoftware.Resettable;
import com.icodesoftware.Session;

public interface Channel extends Configurable, Resettable {

    int channelId();
    int sessionId();
    int timeout();
    byte[] serverKey();

    void write(Session.Header messageHeader, byte[] payload);

    Connection connection();

    void close();

    void register(Session session,ChannelListener channelListener,UDPEndpointServiceProvider.RequestListener requestListener,UDPEndpointServiceProvider.ActionListener actionListener, Session.TimeoutListener timeoutListener);

}

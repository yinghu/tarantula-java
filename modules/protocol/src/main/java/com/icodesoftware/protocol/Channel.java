package com.icodesoftware.protocol;

import com.icodesoftware.*;

public interface Channel extends Configurable, Closable {

    int channelId();
    int sessionId();
    int timeout();
    byte[] serverKey();

    void write(Session.Header messageHeader, byte[] payload);

    Connection connection();

    void register(Session session,ChannelListener channelListener,UDPEndpointServiceProvider.RequestListener requestListener,UDPEndpointServiceProvider.ActionListener actionListener, Session.TimeoutListener timeoutListener);

}

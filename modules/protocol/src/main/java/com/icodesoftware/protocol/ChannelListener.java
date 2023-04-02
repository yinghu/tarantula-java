package com.icodesoftware.protocol;

import com.icodesoftware.protocol.Channel;

public interface ChannelListener {

    void onValidated(Channel channel);
    void onJoined(Channel channel);
    void onLeft(Channel channel);
}

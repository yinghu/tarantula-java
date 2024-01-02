package com.icodesoftware.protocol;

public interface ChannelListener {

    void onValidated(Channel channel);
    void onJoined(Channel channel);
    void onLeft(Channel channel);

}

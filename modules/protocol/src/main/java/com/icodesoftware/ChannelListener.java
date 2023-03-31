package com.icodesoftware;

public interface ChannelListener {

    void onValidated(Channel channel);
    void onJoined(Channel channel);
    void onLeft(Channel channel);
}

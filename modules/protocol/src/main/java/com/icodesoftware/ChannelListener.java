package com.icodesoftware;

public interface ChannelListener {

    void onJoined(Channel channel);
    void onLeft(Channel channel);
}

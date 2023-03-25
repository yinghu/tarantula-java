package com.icodesoftware.protocol;

public interface GameModule extends UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener{

    void setup(GameServiceProvider gameServiceProvider);
}

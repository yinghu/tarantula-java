package com.icodesoftware.protocol;

import com.icodesoftware.service.EndPoint;

public interface UDPEndpointServiceProvider extends EndPoint,Runnable,Messenger{

    void daemon(boolean daemon);
    void registerUserChannel(UserChannel userChannel);

}

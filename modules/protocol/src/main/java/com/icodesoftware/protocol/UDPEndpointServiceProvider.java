package com.icodesoftware.protocol;

import com.icodesoftware.service.EndPoint;

public interface UDPEndpointServiceProvider extends EndPoint,Runnable,Messenger{

    void daemon(boolean daemon);
    void registerUserChannel(UserChannel userChannel);
    UserChannel releaseUserChannel(int channelId);
    void registerPingListener(PingListener pingListener);

    interface SessionListener{
        void onTimeout(int channelId,int sessionId);
    }

    interface UserSessionValidator {
        boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    interface RequestListener{
        byte[] onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    interface PingListener{
        void onPing();
    }


}

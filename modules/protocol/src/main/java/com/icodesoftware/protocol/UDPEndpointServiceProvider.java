package com.icodesoftware.protocol;

import com.icodesoftware.service.EndPoint;

public interface UDPEndpointServiceProvider extends EndPoint,Runnable,Messenger{

    void daemon(boolean daemon);
    void registerUserChannel(UserChannel userChannel);
    void releaseUserChannel(UserChannel userChannel);

    interface SessionListener{
        void onTimeout(int channelId,int sessionId);
    }

    interface UserSessionValidator {
        boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    interface RequestListener{
        byte[] onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

}

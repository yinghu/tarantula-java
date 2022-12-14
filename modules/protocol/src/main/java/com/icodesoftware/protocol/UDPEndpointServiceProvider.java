package com.icodesoftware.protocol;

import com.icodesoftware.service.EndPoint;

public interface UDPEndpointServiceProvider extends EndPoint,Runnable,Messenger{

    int SESSION_CHECK_INTERVAL = 5000;
    int SERVER_PING_INTERVAL = 3000;
    int RETRY_TIMEOUT = 200;
    int PENDING_ACTION_INTERVAL = 50;
    int SLEEP_TIME_OUT = 5;

    int CONNECTION_HEALTHY_CHECK_RETRIES = 3;

    void daemon(boolean daemon);
    void sessionTimeout(int timeout);
    int sessionTimeout();
    void receiverTimeout(int timeout);
    void retryInterval(int interval);
    void registerUserChannel(UserChannel userChannel);
    UserChannel releaseUserChannel(int channelId);
    void registerPingListener(PingListener pingListener);

    boolean onOutboundMessage();
    boolean onReceiveMessage();

    void onTimer();

    interface SessionListener{
        void onTimeout(int channelId,int sessionId);
    }

    interface UserSessionValidator {
        boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    interface RequestListener{
        byte[] onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    //ping game cluster server
    interface PingListener{
        void onPing();
    }
}

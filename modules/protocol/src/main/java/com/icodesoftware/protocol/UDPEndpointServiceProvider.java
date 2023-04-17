package com.icodesoftware.protocol;

import com.icodesoftware.Session;
import com.icodesoftware.service.EndPoint;

public interface UDPEndpointServiceProvider extends EndPoint,Messenger{

    int GAME_SESSION_TIME_OUT = 10000; //10s

    int PING_LISTENER_INTERVAL = 5000; //5s

    int CLIENT_PING_INTERVAL = 3000;//3s

    int RETRY_INTERVAL = 250;//250ms

    int FRAME_RATE = 50; //50ms

    int SLEEP_TIME_OUT = 5; //5ms

    int UDP_RECEIVE_TIME_OUT = 3000; //3s

    int CONNECTION_HEALTHY_CHECK_RETRIES = 3;

    int RECEIVER_THREAD_PRIORITY = 8;
    int SENDER_THREAD_PRIORITY = 8;


    void sessionTimeout(int timeout);
    int sessionTimeout();

    void receiverTimeout(int timeout);
    void retryInterval(int interval);
    void pingListenerInterval(int interval);
    void pingClientInterval(int interval);

    <T extends UserChannel> void registerUserChannel(T userChannel);

    UserChannel releaseUserChannel(int channelId);

    void registerPingListener(PingListener pingListener);
    void registerCipherListener(CipherListener cipherListener);
    CipherListener registerCipherListener(byte[] key);
    //single outbound message sent out
    boolean onOutboundMessage();

    //single inbound message received
    boolean onReceiveMessage();

    //single timer loop
    void onTimer(int frameRate);

    interface SessionListener{
        void onLeft(int channelId,int sessionId);
        void onJoined(int channelId,int sessionId);
    }

    interface UserSessionValidator {
        boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    interface RequestListener{
        byte[] onRequest(Session session,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer);
    }

    interface ActionListener{
        void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer,RelayListener callback);
    }

    //ping game cluster from udp server
    interface PingListener{
        void onPing();
    }

    interface RelayListener{
        void onRelay(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }

    interface CipherListener{
        boolean decrypt(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
        boolean encrypt(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }
}

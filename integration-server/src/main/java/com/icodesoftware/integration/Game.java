package com.icodesoftware.integration;

import com.icodesoftware.integration.channel.RemoteSession;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public interface Game {

    String zoneId();
    String roomId();
    boolean started();

    //customized game event callback
    void onAction(InboundMessage inboundMessage);


    //predefined game event callbacks
    void onCollision(InboundMessage inboundMessage);
    void onJoin(int sessionId,RemoteSession remoteSession);
    void onLeave(int sessionId);
    void onLoad(InboundMessage inboundMessage);



    //server push event callbacks
    void onSpec(DataBuffer dataBuffer);
    void onStart();
    void onClosing();
    void onClose();
    void onEnd();
    void onOvertime();
    void onJoinTimeout();

    void registerGameChannelListener(GameChannel.Listener listener);
}

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

    //customized game event callback
    void onAction(InboundMessage inboundMessage);


    //predefined game event callbacks
    void onJoin(int sessionId,RemoteSession remoteSession);
    void onLeave(int sessionId);
    void onLoad(InboundMessage inboundMessage);

    boolean onSpawn(InboundMessage inboundMessage);
    boolean onCollision(InboundMessage inboundMessage);
    boolean onDestroy(InboundMessage inboundMessage);
    boolean onMove(InboundMessage inboundMessage);
    boolean onSync(InboundMessage inboundMessage);

    //server push event callbacks
    void onSpec(DataBuffer dataBuffer);
    void onStart();
    void onClosing();
    void onClose();
    void onOvertime();
    void onJoinTimeout();

    void registerGameChannelListener(GameChannel.Listener listener);

    void onGameLoop();
}

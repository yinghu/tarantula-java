package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannel {

    long channelId();
    boolean started();
    void onMessage(InboundMessage pendingInboundMessage);

    void join(int seat,int sessionId,int[] messageRange, SocketAddress socketAddress);
    void leave(int sessionId, SocketAddress socketAddress);
    void relay(int messageId,boolean ack,MessageHandler messageHandler,OutboundMessage pendingOutboundMessage);
    void relay(int sessionId,int messageId,boolean ack,MessageHandler messageHandler,OutboundMessage pendingOutboundMessage);
    void ack(int sessionId,int messageId,SocketAddress source);
    void ack(int sessionId,int messageId);

    void ping();
    void pong(int sessionId);

    void retry();
    void pending(int sessionId, int messageId, byte[] pending, MessageHandler callback);
    void pending(SocketAddress socketAddress,int messageId,byte[] pending);

    void onGame(Game game);
    Game onGame();
    int totalRetries();

    void onSession(int sessionId,OnSession onSession);
    void onSession(OnSession onSession);

    void clear();

    interface Listener{
        void onChannelClosed(GameChannel channelClosed);
    }
    interface OnSession{
        void execute(GameSession gameSession);
    }

}

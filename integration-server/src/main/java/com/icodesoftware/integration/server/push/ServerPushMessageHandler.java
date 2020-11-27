package com.icodesoftware.integration.server.push;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.AbstractMessageHandler;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class ServerPushMessageHandler extends AbstractMessageHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ServerPushMessageHandler.class);

    private ConcurrentHashMap<SocketAddress,FIFOBuffer<Integer>> ackBuffers;

    private final GameSpecHandler gameSpecHandler;

    public ServerPushMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
        ackBuffers = new ConcurrentHashMap<>();
        gameSpecHandler = new GameSpecHandler(gameChannelService);
    }
    @Override
    public int type() {
        return SERVER_PUSH;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        FIFOBuffer<Integer> ackBuffer = ackBuffers.computeIfAbsent(pendingInboundMessage.source(),(k)->new FIFOBuffer(20,new Integer[20]));
        if(pendingInboundMessage.ack()) {
            ackBuffer.push(pendingInboundMessage.messageId());
            OutboundMessage ack = new OutboundMessage();
            ack.ack(false);
            ack.type(MessageHandler.ACK);
            ack.sequence(0);
            List<Integer> alist = ackBuffer.list(new ArrayList<>());
            DataBuffer dataBuffer = new DataBuffer();
            dataBuffer.putInt(alist.size());
            alist.forEach((a)-> dataBuffer.putInt(a));
            ack.payload(dataBuffer.toArray());
            gameChannelService.pendingOutbound(ByteBuffer.wrap(gameChannelService.encode(ack)), pendingInboundMessage.source());
        }
        int seq = pendingInboundMessage.sequence();
        log.warn("Server push->["+seq+"] from ["+pendingInboundMessage.source()+"]");
        switch (seq){
            case GAME_SPEC:
                gameSpecHandler.onMessage(pendingInboundMessage);
                break;
            case GAME_START:
                onGameStart(pendingInboundMessage);
                break;
            case GAME_CLOSING:
                onGameClosing(pendingInboundMessage);
                break;
            case GAME_CLOSE:
                onGameClose(pendingInboundMessage);
                break;
            case GAME_END:
                onGameEnd(pendingInboundMessage);
                break;
            case GAME_JOIN_TIMEOUT:
                onGameJoinTimeout(pendingInboundMessage);
                break;
            case GAME_OVERTIME:
                onGameOvertime(pendingInboundMessage);
                break;
            default:
                MessageHandler discharge = gameChannelService.messageHandler(MessageHandler.DISCHARGE);
                discharge.onMessage(pendingInboundMessage);
        }
    }
    private void onGameJoinTimeout(InboundMessage inboundMessage){
        GameJoinTimeoutHandler gameJoinTimeoutHandler = new GameJoinTimeoutHandler(this.gameChannelService);
        gameJoinTimeoutHandler.onMessage(inboundMessage);
        gameJoinTimeoutHandler.relay();
    }
    private void onGameStart(InboundMessage inboundMessage){
        GameStartHandler gameJoinTimeoutHandler = new GameStartHandler(this.gameChannelService);
        gameJoinTimeoutHandler.onMessage(inboundMessage);
        gameJoinTimeoutHandler.relay();
    }
    private void onGameClosing(InboundMessage inboundMessage){
        GameClosingHandler gameJoinTimeoutHandler = new GameClosingHandler(this.gameChannelService);
        gameJoinTimeoutHandler.onMessage(inboundMessage);
        gameJoinTimeoutHandler.relay();
    }
    private void onGameOvertime(InboundMessage inboundMessage){
        GameOvertimeHandler gameJoinTimeoutHandler = new GameOvertimeHandler(this.gameChannelService);
        gameJoinTimeoutHandler.onMessage(inboundMessage);
        gameJoinTimeoutHandler.relay();
    }
    private void onGameClose(InboundMessage inboundMessage){
        GameCloseHandler gameJoinTimeoutHandler = new GameCloseHandler(this.gameChannelService);
        gameJoinTimeoutHandler.onMessage(inboundMessage);
        gameJoinTimeoutHandler.relay();
    }
    private void onGameEnd(InboundMessage inboundMessage){
        GameEndHandler gameJoinTimeoutHandler = new GameEndHandler(this.gameChannelService);
        gameJoinTimeoutHandler.onMessage(inboundMessage);
        gameJoinTimeoutHandler.relay();
    }
}

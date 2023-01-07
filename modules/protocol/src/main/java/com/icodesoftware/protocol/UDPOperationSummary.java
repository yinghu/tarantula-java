package com.icodesoftware.protocol;

import java.util.concurrent.atomic.AtomicInteger;

public class UDPOperationSummary {

    public static String PENDING_INBOUND_MESSAGE_NUMBER = "pendingInboundMessageNumber";
    public static String PENDING_OUTBOUND_MESSAGE_NUMBER = "pendingOutboundMessageNumber";
    public static String PENDING_BUFFER_NUMBER = "pendingBufferNumber";
    public static String PENDING_MESSAGE_BUFFER_NUMBER = "pendingMessageBufferNumber";
    public static String USER_CHANNEL_NUMBER = "userChannelNumber";

    public static final String PENDING_UDP_SESSION_SIZE = "pendingUdpSessionSize";
    public static final String UDP_GAME_SESSION_SIZE = "gameUdpSessionSize";

    public AtomicInteger pendingInboundMessageNumber = new AtomicInteger(0);
    public AtomicInteger pendingOutboundMessageNumber = new AtomicInteger(0);
    public AtomicInteger pendingBufferNumber = new AtomicInteger(0);
    public AtomicInteger pendingMessageBufferNumber = new AtomicInteger(0);
    public AtomicInteger userChannelNumber = new AtomicInteger(0);
}

package com.icodesoftware.integration.channel;

/**
 * Created by yinghu lu on 10/23/2020.
 */
public class PendingMessageIndex {
    public final int sessionId;
    public final int messageId;
    public PendingMessageIndex(int sessionId,int messageId){
        this.sessionId = sessionId;
        this.messageId = messageId;
    }
    @Override
    public int hashCode(){
        return sessionId+messageId;
    }
    @Override
    public boolean equals(Object obj){
        PendingMessageIndex pendingMessageIndex = (PendingMessageIndex)obj;
        return sessionId==pendingMessageIndex.sessionId&&messageId==pendingMessageIndex.messageId;
    }
}

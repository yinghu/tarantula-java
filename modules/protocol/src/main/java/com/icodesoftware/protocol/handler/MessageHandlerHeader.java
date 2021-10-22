package com.icodesoftware.protocol.handler;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.Messenger;

public class MessageHandlerHeader implements MessageHandler {

    protected int type;
    protected Messenger messenger;

    public MessageHandlerHeader(int type,Messenger messenger){
        this.type = type;
        this.messenger = messenger;
    }
    @Override
    public int type() {
        return type;
    }

    public void onMessage(MessageBuffer.MessageHeader messageHeader, byte[] payload){

    }
}

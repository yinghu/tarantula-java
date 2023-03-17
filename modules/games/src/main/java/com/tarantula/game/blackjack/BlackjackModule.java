package com.tarantula.game.blackjack;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;

public class BlackjackModule implements Module, UDPEndpointServiceProvider.RequestListener {

    private ApplicationContext context;

    @Override
    public void onJoin(Session session) throws Exception{
        //session.write(ret,this.label());
    }



    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Item service started", OnLog.INFO);
    }

    @Override
    public void clear(){

    }

    @Override
    public byte[] onRequest(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        System.out.println("blackjack request");
        return null;
    }
}

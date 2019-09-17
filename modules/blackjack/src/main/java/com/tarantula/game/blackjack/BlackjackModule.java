package com.tarantula.game.blackjack;

import com.tarantula.*;
import com.tarantula.Module;

public class BlackjackModule implements Module {

    private ApplicationContext context;



    @Override
    public void onJoin(Session session, Connection connection) throws Exception{
        //session.write(ret,this.label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        session.write(payload,label());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Item service started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "item";
    }
    @Override
    public void clear(){

    }
}

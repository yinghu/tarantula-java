package com.tarantula.game.blackjack;

import com.icodesoftware.*;
import com.icodesoftware.Module;

public class BlackjackModule implements Module {

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
}

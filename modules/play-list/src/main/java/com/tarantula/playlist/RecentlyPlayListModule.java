package com.tarantula.playlist;


import com.icodesoftware.*;
import com.icodesoftware.Module;

public class RecentlyPlayListModule implements Module {

    private ApplicationContext context;

    @Override
    public boolean onRequest(Session session, byte[] payload, Module.OnUpdate onUpdate) throws Exception {
        session.write(payload,label());
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.context.log("Recently Play List deployed on tag->"+context.descriptor().tag(), OnLog.WARN);
    }

    @Override
    public String label() {
        return "presence";
    }

    @Override
    public void onConnection(Connection connection){
        this.context.log(">>>>>>"+connection.type(),OnLog.WARN);
    }
}

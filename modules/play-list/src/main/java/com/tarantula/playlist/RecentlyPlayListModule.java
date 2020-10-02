package com.tarantula.playlist;


import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.OnLog;
import com.icodesoftware.Session;

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
        return "playlist";
    }
}

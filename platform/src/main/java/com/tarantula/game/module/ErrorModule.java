package com.tarantula.game.module;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Module;
import com.icodesoftware.Session;

public class ErrorModule implements Module {

    public final static ErrorModule ERROR_MODULE = new ErrorModule();


    @Override
    public void onJoin(Session session) throws Exception{
        throw new RuntimeException("Module not available");
    }

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        throw new RuntimeException("Module not available");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {

    }
}

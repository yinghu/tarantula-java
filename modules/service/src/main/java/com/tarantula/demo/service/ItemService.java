package com.tarantula.demo.service;

import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.Session;

public class ItemService implements Module {

    private ApplicationContext context;

    @Override
    public void onJoin(Session session) throws Exception{
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
    }

    @Override
    public String label() {
        return "itemService";
    }
    @Override
    public void clear(){

    }
}

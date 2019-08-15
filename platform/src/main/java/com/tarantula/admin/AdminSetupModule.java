package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class AdminSetupModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DBObject dbObject;
    @Override
    public void onJoin(Session session) throws Exception {
        session.write(this.builder.create().toJson(dbObject.setup()).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action(),OnLog.INFO);
        session.write(payload,label());
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(DBObject.class,new DBObjectSerializer());
        this.dbObject = new DBObject();
        this.dbObject.successful(true);
        this.dbObject.name("setup tool");
        this.context.log("Admin setup module started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "admin";
    }
}

package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.ApplicationContext;
import com.tarantula.Module;
import com.tarantula.OnLog;
import com.tarantula.Session;

public class AdminMonitorModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private AdminMonitorObject dbObject;

    public void onJoin(Session session) throws Exception{
        session.write(this.builder.create().toJson(dbObject).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action(),OnLog.INFO);
        session.write(payload,label());
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(AdminMonitorObject.class,new AdminObjectSerializer());
        this.dbObject = new AdminMonitorObject(this.label());
        this.context.log("Admin monitor module started", OnLog.INFO);
    }
    public void onTimer(OnUpdate update){
        update.on(this.builder.create().toJson(dbObject).getBytes());
    }
    @Override
    public String label() {
        return "admin-monitor";
    }
}

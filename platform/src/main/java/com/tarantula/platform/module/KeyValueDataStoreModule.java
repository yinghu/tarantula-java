package com.tarantula.platform.module;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.util.ResponseSerializer;

public class KeyValueDataStoreModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore dataStore;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onSet")){
            String key = session.systemId()+Recoverable.PATH_SEPARATOR+session.name();
            dataStore.set(key.getBytes(),payload);
            ResponseHeader resp = new ResponseHeader("onSet","Saved on key ["+key+"]",true);
            session.write(builder.create().toJson(resp).getBytes(),label());
        }
        else if(session.action().equals("onGet")){
            String key = session.systemId()+Recoverable.PATH_SEPARATOR+session.name();
            byte[] v = dataStore.get(key.getBytes());
            session.write(v!=null?v:"{}".getBytes(),label());
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.dataStore = this.context.dataStore(this.context.descriptor().typeId());
        this.context.log("Data store ["+this.context.descriptor().name()+" started ]", OnLog.WARN);
    }
    @Override
    public String label() {
        return "game-data";
    }
}

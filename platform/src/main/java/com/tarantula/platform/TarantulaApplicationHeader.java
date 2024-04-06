package com.tarantula.platform;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.util.ResponseHeader;
import com.tarantula.platform.util.*;

public class TarantulaApplicationHeader implements TarantulaApplication {

    protected Descriptor descriptor;
    protected ApplicationContext context;
    protected GsonBuilder builder;

    public void callback(Session session, byte[] payload) throws Exception {

    }

    public void onError(Session session, Exception ex) {
        this.context.log(session.toString(),ex, OnLog.ERROR);
        String msg = ex.getMessage()!=null?ex.getMessage():"Unexpected error";
        session.write(this.builder.create().toJson(new ResponseHeader("onError",false,400,msg,"error")).getBytes());
    }


    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
    }


    public Descriptor descriptor() {
        return this.descriptor;
    }


    public void descriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public boolean onEvent(Event event) {
        this.context.log("MISSING EVENT->"+event.toString(),OnLog.WARN);
        return false;
    }


    public String onLobby(){
        return this.descriptor.tag();
    }

    public void onBucket(int bucket,int state) {

    }
}

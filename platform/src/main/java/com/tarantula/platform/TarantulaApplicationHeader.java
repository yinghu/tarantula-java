package com.tarantula.platform;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Connection;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Session;
import com.tarantula.*;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.statistics.StatisticsSerializer;
import com.tarantula.platform.util.*;

/**
 * Updated by yinghu on 6/15/19
 */
public class TarantulaApplicationHeader implements TarantulaApplication,InstanceRegistry.Listener {

    protected Descriptor descriptor;
    protected ApplicationContext context;
    protected GsonBuilder builder;

    public void callback(Session session, byte[] payload) throws Exception {

    }

    public void onError(Session session, Exception ex) {
        this.context.log(session.toString(),ex,OnLog.ERROR);
        String msg = ex.getMessage()!=null?ex.getMessage():"Unexpected error";
        session.write(this.builder.create().toJson(new ResponseHeader("onError",false,400,msg,"error")).getBytes(),"error");
    }


    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
        this.builder.registerTypeAdapter(StatisticsIndex.class,new StatisticsSerializer());
        this.builder.registerTypeAdapter(SessionIdle.class,new SessionIdleSerializer());
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

    public void onRegistry(InstanceRegistry instanceRegistry){

    }
    public String onLobby(){
        return this.descriptor.tag();
    }

    public void onBucket(int bucket,int state) {

    }
    public String typeId(){
        return this.descriptor.typeId();
    }
    public void onState(Connection onConnection){

    }
    public void refund(String systemId,String applicationId){
        Descriptor desc = this.context.descriptor(applicationId);
        OnBalanceTrack onBalanceTrack = new OnBalanceTrack(systemId,desc.entryCost());
        this.context.postOffice().onTag(Presence.LOBBY_TAG).send(systemId,onBalanceTrack);
    }
}

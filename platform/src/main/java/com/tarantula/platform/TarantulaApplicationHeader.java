package com.tarantula.platform;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.platform.util.*;

/**
 * Updated by yinghu on 6/15/19
 */
public class TarantulaApplicationHeader implements TarantulaApplication,InstanceRegistry.Listener {

    protected Descriptor descriptor;
    protected ApplicationContext context;
    protected GsonBuilder builder;
    //protected Connection onConnection;
    public void callback(Session session, byte[] payload) throws Exception {

    }

    public void onError(Session session, Exception ex) {
        //this.context.log(session.toString(),ex,OnLog.ERROR);
        String msg = ex.getMessage()!=null?ex.getMessage():"Unexpected error";
        session.write(this.builder.create().toJson(new ResponseHeader("onError",false,400,msg,"error")).getBytes(),this.descriptor.responseLabel());
    }


    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
        this.builder.registerTypeAdapter(DeltaStatistics.class,new StatisticsSerializer());
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
    public void onState(Connection onConnection){
        //this.onConnection = onConnection;
    }
    public void refund(String systemId,String applicationId){
        Descriptor desc = this.context.descriptor(applicationId);
        OnBalanceTrack onBalanceTrack = new OnBalanceTrack(systemId,desc.entryCost());
        this.context.postOffice().onTag(Presence.LOBBY_TAG).send(systemId,onBalanceTrack);
    }
    public void refund(String systemId,double balance){
        OnBalanceTrack onBalanceTrack = new OnBalanceTrack(systemId,balance);
        this.context.postOffice().onTag(Presence.LOBBY_TAG).send(systemId,onBalanceTrack);
    }
}

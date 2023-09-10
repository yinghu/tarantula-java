package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.service.EventService;
import com.icodesoftware.util.FIFOBuffer;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.event.*;
import java.util.Map;
import com.icodesoftware.util.RecoverableObject;

public class PresenceIndex extends RecoverableObject implements Presence {

    private int counter;
    private boolean local = true;
    private EventService eventService;

    private FIFOBuffer<OnSessionTrack> sessions;

    private DataStore sessionDataStore;
    public PresenceIndex(){

    }

    public PresenceIndex(DataStore sessionDataStore){
        this.sessionDataStore = sessionDataStore;
    }

    public void registerEventService(EventService eventService){
        this.eventService = eventService;
    }

    public Response onPlay(Session session,Descriptor desc){
        fastJoin(session,desc,session.payload());
        return null;
    }
    private void fastJoin(Session session,Descriptor desc,byte[] payload){
        SessionForward fd = new SessionForward(session.source(),session.sessionId());
        FastPlayEvent fe = new FastPlayEvent(fd);
        fe.tournamentId(session.tournamentId());
        fe.distributionId(session.distributionId());
        fe.stub(session.stub());
        fe.routingNumber(session.routingNumber());
        fe.ticket(session.ticket());
        fe.name(session.name());
        fe.clientId(session.clientId());
        fe.payload(payload);
        RoutingKey rk = this.eventService.routingKey(session.distributionId(),desc.tag());//route to player node
        fe.destination(rk.route());//node/tag/partition
        this.eventService.onEvent(fe);
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.PRESENCE_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("2",counter);
        this.properties.put("3",disabled);
        this.properties.put("4",this.timestamp);
        this.properties.put("5",this.local);
        this.properties.put("6",this.index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.counter = ((Number)properties.getOrDefault("2",0)).intValue();
        this.disabled = (Boolean)properties.getOrDefault("3",false);
        this.timestamp = ((Number)properties.getOrDefault("4",0)).longValue();
        this.local = (Boolean)properties.getOrDefault("5",true);
        this.index = (String)properties.get("6");
    }
    public boolean write(DataBuffer buffer){
        buffer.writeInt(counter);
        buffer.writeBoolean(local);
        buffer.writeLong(timestamp);
        buffer.writeBoolean(disabled);
        return true;
    }
    public boolean read(DataBuffer buffer) {
        this.counter = buffer.readInt();
        this.local = buffer.readBoolean();
        this.timestamp = buffer.readLong();
        this.disabled = buffer.readBoolean();
        return true;
    }

    public boolean online(){
        this.timestamp = System.currentTimeMillis();
        return (!this.disabled);
    }
    public boolean local(){
        return local;
    }

    public OnSession stub(){
        OnSessionTrack onSessionTrack = sessions.pop();
        if(onSessionTrack==null) return OnSessionTrack.SESSION_NOT_AVAILABLE;
        return new OnSessionTrack(distributionId,onSessionTrack.distributionId());
    }

    public boolean offSession(long stub){
        OnSessionTrack onSessionTrack = new OnSessionTrack();
        onSessionTrack.distributionId(stub);
        if(this.sessionDataStore.load(onSessionTrack)){
            sessions.push(onSessionTrack);
        }
        return true;
    }
    @Override
    public String toString(){
        return "On Presence ["+this.distributionId()+"/"+timestamp+"/"+counter+"/"+disabled+"]";
    }

    public void load(int max){
        sessions = new FIFOBuffer<>(max,new OnSessionTrack[max]);
        sessionDataStore.list(new OnSessionQuery<OnSessionTrack>(key()),t->{
            sessions.push(t);
            return true;
        });
    }
}

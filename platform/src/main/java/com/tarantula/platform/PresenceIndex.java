package com.tarantula.platform;

import com.icodesoftware.*;
import com.icodesoftware.protocol.session.OnSessionTrack;
import com.icodesoftware.service.EventService;
import com.icodesoftware.util.FIFOBuffer;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.event.*;
import com.icodesoftware.util.RecoverableObject;

public class PresenceIndex extends RecoverableObject implements Presence {

    private int counter;
    private boolean local = true;
    private EventService eventService;

    private FIFOBuffer<SessionIndex> sessions;

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
        SessionIndex onSessionTrack = sessions.pop();
        if(onSessionTrack==null) return OnSessionTrack.SESSION_NOT_AVAILABLE;
        counter++;
        this.update();
        return new OnSessionTrack(distributionId,onSessionTrack.distributionId());
    }

    public boolean offSession(long stub){
        SessionIndex onSessionTrack = new SessionIndex();
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
        sessions = new FIFOBuffer<>(max,new SessionIndex[max]);
        sessionDataStore.list(new OnSessionQuery(key()),t->{
            sessions.push(t);
            return true;
        });
    }
}

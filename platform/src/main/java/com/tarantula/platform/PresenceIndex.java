package com.tarantula.platform;

import com.tarantula.*;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.event.*;
import java.nio.ByteBuffer;
/**
 * Updated by yinghu on 4/30/2020
 */
public class PresenceIndex extends RecoverableObject implements Presence {
    private double balance;
    private int counter;
    private EventService eventService;
    public PresenceIndex(double initialBalance){
        this();
        this.balance = initialBalance;
    }


    public PresenceIndex(){
        this.binary = true;
        this.vertex = "Presence";
    }
    public synchronized double balance() {
        return this.balance;
    }
    public synchronized boolean transact(double delta) {
        if((balance+(delta)>=0)){
            balance = balance+(delta);
            this.update();
            return true;
        }else{
            return false;
        }
    }
    public void registerEventService(EventService eventService){
        this.eventService = eventService;
    }
    public Response onPlay(Session session,Descriptor desc){
        Response resp = null;
        if(this.transact(desc.entryCost()*(-1))){
            fastJoin(session,desc,new byte[0]);
        }
        else{
            resp = new ResponseHeader("onPlay",false,Response.INSUFFICIENT_BALANCE,"not enough balance","error");
        }
        return resp;
    }
    private void fastJoin(Session session,Descriptor desc,byte[] payload){
        SessionForward fd = new SessionForward(session.source(),session.sessionId());
        FastPlayEvent fe = new FastPlayEvent(desc.distributionKey(),fd);
        fe.systemId(session.systemId());
        fe.stub(session.stub());
        fe.routingNumber(session.routingNumber());
        fe.accessMode(Session.FAST_PLAY_MODE);
        fe.balance(desc.entryCost());
        fe.ticket(session.ticket());
        fe.forwarding(true);
        fe.payload(payload);
        RoutingKey rk = this.eventService.routingKey(session.systemId(),desc.tag());//route to player node
        fe.destination(rk.route());
        this.eventService.publish(fe);
    }
    public Response onPlay(Session session,OnAccess onAccess,Descriptor desc){
        Response resp = null;
        if(this.transact(desc.entryCost() * (-1))){
            switch (onAccess.accessMode()){
                case Session.FAST_PLAY_MODE:
                    //distributed on application Id
                    fastJoin(session,desc,onAccess.payload());
                    break;
                case Session.INSTANCE_PLAY_MODE:
                    SessionForward fxd = new SessionForward(session.source(),session.sessionId());
                    InstancePlayEvent onInstanceEvent = new InstancePlayEvent(onAccess.applicationId(),onAccess.instanceId(),fxd);
                    onInstanceEvent.systemId(session.systemId());
                    onInstanceEvent.balance(desc.entryCost());
                    onInstanceEvent.stub(session.stub());
                    onInstanceEvent.routingNumber(session.routingNumber());
                    onInstanceEvent.accessMode(Session.INSTANCE_PLAY_MODE);
                    onInstanceEvent.ticket(session.ticket());//session presence OID embedded in token
                    onInstanceEvent.payload(onAccess.payload());
                    RoutingKey rk2 = this.eventService.routingKey(onAccess.instanceId(),desc.tag(),partitionFromInstanceId(onAccess.instanceId())); //route to the partition of the instance node
                    onInstanceEvent.destination(rk2.route());
                    this.eventService.publish(onInstanceEvent);
                    break;
                default:
                    resp = new ResponseHeader("onPlay",false,Response.ACCESS_MODE_NOT_SUPPORTED,"Access Mode ["+onAccess.accessMode()+"] not supported","error");
            }
        }else{
            resp = new ResponseHeader("onPlay",false,Response.INSUFFICIENT_BALANCE,"not enough balance","error");
        }
        return resp;
    }

    private int partitionFromInstanceId(String tid){
        return Integer.parseInt(tid.substring(tid.lastIndexOf(Recoverable.PATH_SEPARATOR)+1));
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
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putLong(timestamp);
        buffer.putDouble(balance);
        buffer.putInt(counter);
        buffer.putInt(disabled?1:0);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.timestamp = buffer.getLong();
        this.balance = buffer.getDouble();
        this.counter = buffer.getInt();
        this.disabled = buffer.getInt()==1;
    }
    public int count(int delta){
        this.disabled = false;
        this.timestamp = System.currentTimeMillis();
        return (counter = counter + delta);
    }
    public boolean online(){
        this.timestamp = System.currentTimeMillis();
        return (!this.disabled);
    }
    @Override
    public String toString(){
        return "On Presence ["+this.distributionKey()+"/"+timestamp+"/"+balance+"/"+counter+"/"+disabled+"]";
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}

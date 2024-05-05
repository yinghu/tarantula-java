package com.tarantula.platform.event;
import com.hazelcast.nio.serialization.Portable;
import com.icodesoftware.Session;
import com.icodesoftware.service.EventService;
import com.icodesoftware.util.OnApplicationHeader;

abstract public class Data extends OnApplicationHeader implements Portable{

    protected transient String destination;

    protected int retries;


    protected transient Portable portable;

    protected transient String tag;


    protected transient SessionForward forward;

    protected transient EventService eventService;

    protected transient int routingNumber;

    public int routingNumber(){
        return this.routingNumber;
    }
    public void routingNumber(int routingNumber){
        this.routingNumber = routingNumber;
    }



    public String destination() {
        return this.destination;
    }

    public void destination(String  destination) {
        this.destination  = destination;
    }

    @Override
    public long systemId() {
        return distributionId;
    }
    public int retries() {
        return this.retries;
    }

    public void retries(int retries) {
        this.retries = retries;
    }


    public Portable portable(){
        return this.portable;
    }
    public void portable(Portable portable){
        this.portable = portable;
    }
    public String tag(){
        return this.tag;
    }
    public void tag(String tag){
        this.tag = tag;
    }

    public void eventService(EventService eventService){
        this.eventService = eventService;
    }

    public void write(byte[] delta,int batch,String contentType){
        this.write(delta,batch,contentType,false);
    }

    public void write(byte[] payload,int batch,String contentType,boolean closed){
        this.eventService.publish(new ResponsiveEvent(this.source,this.sessionId,payload,batch,contentType,closed));
    }

    public void write(byte[] message){
        this.write(message,false);
    }
    public void write(byte[] payload,boolean closed){
        this.eventService.publish(new ResponsiveEvent(this.source,this.sessionId,payload,closed));
    }
    public void write(Session.Header messageHeader, byte[] payload){};

}

package com.tarantula.platform.event;
import com.hazelcast.nio.serialization.Portable;
import com.tarantula.EventService;
import com.tarantula.platform.OnApplicationHeader;

abstract public class Data extends OnApplicationHeader implements Portable{

    protected String sessionId;
    protected boolean joined;

    protected String source;
    protected String destination;

    protected String contentType ="application/json";

    protected byte[] payload;

    protected int retries;


    protected Portable portable;

    protected String tag;

    protected String clientId;

    //protected boolean forwarding;
    protected boolean streaming;
    protected String action;

    protected String trackId;

    protected boolean closed;

    protected SessionForward forward;

    protected EventService eventService;

    protected int routingNumber;
    public int routingNumber(){
        return this.routingNumber;
    }
    public void routingNumber(int routingNumber){
        this.routingNumber = routingNumber;
    }

    public boolean streaming(){
        return this.streaming;
    }
    public void streaming(boolean streaming){
        this.streaming = streaming;
    }
    public boolean closed(){
        return this.closed;
    }
    public void closed(boolean closed){
        this.closed = closed;
    }


    public String sessionId() {
        return this.sessionId;
    }

    public void sessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void joined(boolean joined){
        this.joined = joined;
    }
    public boolean joined(){
        return this.joined;
    }

    public String source() {
        return this.source;
    }

    public void source(String source) {
        this.source = source;
    }

    public String destination() {
        return this.destination;
    }

    public void destination(String  dest) {
        this.destination  = dest;
    }

    public int retries() {
        return this.retries;
    }

    public void retries(int retries) {
        this.retries = retries;
    }

    public byte[] payload() {
        return this.payload;
    }

    public void payload(byte[] data) {
        this.payload = data;
    }
    public String contentType(){
        return this.contentType;
    }
    public void contentType(String contentType){
        this.contentType = contentType;
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
    public String clientId(){
        return this.clientId;
    }
    public void clientId(String clientId){
        this.clientId =clientId;
    }

    public String action(){
        return this.action;
    }
    public void action(String action){
        this.action =action;
    }


    public String trackId(){
        return this.trackId;
    }
    public void trackId(String trackId){
        this.trackId = trackId;
    }

    public void eventService(EventService eventService){
        this.eventService = eventService;
    }

    public void write(byte[] delta,int batch,String contentType,String label){
        this.write(delta,batch,contentType,label,false);
    }

    public void write(byte[] payload,int batch,String contentType,String label,boolean closed){
        this.eventService.publish(new ResponsiveEvent(this.source,this.sessionId,payload,batch,contentType,label,closed));
    }

    public void write(byte[] message,String label){
        this.write(message,label,false);
    }
    public void write(byte[] payload,String label,boolean closed){
        this.eventService.publish(new ResponsiveEvent(this.source,this.sessionId,payload,label,closed));
    }

}

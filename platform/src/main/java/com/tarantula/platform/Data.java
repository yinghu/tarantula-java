package com.tarantula.platform;
import com.hazelcast.nio.serialization.Portable;
import com.tarantula.EventService;
import com.tarantula.platform.event.ResponsiveEvent;

public class Data extends RecoverableObject{


    protected String systemId;
    protected int stub;
    protected String sessionId;
    protected boolean joined;
    protected int accessMode;

    protected String ticket;

    protected String typeId;
    protected String subtypeId;

    protected String source;
    protected String destination;

    protected String name;
    protected String applicationId;
    protected String instanceId;

    protected String contentType ="application/json";

    protected byte[] payload;

    protected int retries;

    protected boolean tournamentEnabled;

    protected double balance;

    protected Portable portable;

    protected String tag;

    protected String clientId;

    protected boolean forwarding;
    protected boolean streaming;
    protected String action;

    protected String trackId;


    protected int level;
    protected String message;
    protected Exception error;


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
    public int level(){
        return this.level;
    }
    public void level(int level){
        this.level = level;
    }
    public String message(){
        return message;
    }
    public void message(String message){
        this.message = message;
    }
    public Exception error(){
        return this.error;
    }
    public void error(Exception error){
        this.error = error;
    }

    public String systemId() {
        return this.systemId;
    }

    public void systemId(String systemId) {
        this.systemId = systemId;
    }
    public int stub(){return this.stub;}
    public void stub(int stub){ this.stub = stub;}
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
    public String typeId(){
        return this.typeId;
    }
    public void typeId(String typeId){
        this.typeId = typeId;
    }
    public String subtypeId(){
        return this.subtypeId;
    }
    public void subtypeId(String subtypeId){
        this.subtypeId = subtypeId;
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
    public boolean tournamentEnabled(){
        return this.tournamentEnabled;
    }
    public void tournamentEnabled(boolean tournamentEnabled){
        this.tournamentEnabled = tournamentEnabled;
    }

    public String name(){
        return this.name;
    }
    public void name(String applicationName){
        this.name = applicationName;
    }
    public String applicationId() {
        return this.applicationId;
    }

    public void applicationId(String appId) {
        this.applicationId= appId;
    }

    public String instanceId() {
        return this.instanceId;
    }

    public void instanceId(String insId) {
        this.instanceId = insId;
    }
    public int accessMode(){
        return this.accessMode;
    }
    public void accessMode(int accessMode){
        this.accessMode = accessMode;
    }
    public String ticket(){
        return this.ticket;
    }
    public void ticket(String ticket){
        this.ticket = ticket;
    }

    public double balance(){
        return this.balance;
    }
    public void balance(double balance){
        this.balance = balance;
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

    public boolean forwarding(){
        return  this.forwarding;
    }
    public void forwarding(boolean forwarding){
       this.forwarding = forwarding;
    }

    public String action(){
        return this.action;
    }
    public void action(String action){
        this.action =action;
    }

    public double entryCost(){
        return this.balance;
    }
    public void entryCost(double entryCost){
        this.balance =entryCost;
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

    public int getFactoryId(){
        return -1;
    }
    public int getClassId(){
        return -1;
    }

}

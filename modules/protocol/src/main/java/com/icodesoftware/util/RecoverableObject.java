package com.icodesoftware.util;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;

import java.util.HashMap;
import java.util.Map;

public class RecoverableObject implements Recoverable {

    protected String bucket;

    protected Key ownerKey;

    protected String owner;
    protected  String label;

    protected  boolean disabled;

    protected  Map<String,Object> properties = new HashMap();

    protected  boolean onEdge;

    protected long timestamp;
    protected long revision;
    protected long distributionId;
    protected int routingNumber;

    protected String index;

    protected String name;
    protected DataStore dataStore;

    public void dataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    public void update() {
        this.dataStore.update(this);
    }
    public long timestamp(){
        return this.timestamp;
    }
    public void timestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public String bucket() {
        return bucket;
    }

    public void bucket(String bucket) {
        this.bucket = bucket;
    }

    public Key ownerKey(){
        return this.ownerKey;
    }
    public void ownerKey(Key ownerKey){
        this.ownerKey = ownerKey;
    }
    public String owner(){
        return this.owner;
    }
    public void owner(String owner){
        this.owner = owner;
    }

    public String label(){
        return this.label;
    }
    public void label(String label){
        this.label = label;
    }
    //on access API

    public String name(){
        return this.name;
    }
    public void name(String name){
        this.name = name;
    }
    public void property(String header,Object value){
        this.properties.put(header,value);
    }
    public Object property(String header){
        return this.properties.get(header);
    }
    public Map<String,Object> toMap(){
        return this.properties;
    }
    public void fromMap(Map<String,Object> properties){
        properties.forEach((String k,Object v)->this.properties.put(k,v));
    }
    final public byte[] toBinary(){
        return JsonUtil.toJson(toMap());
    }
    final public void fromBinary(byte[] payload){
        fromMap(JsonUtil.toMap(payload));
    }
    public boolean backup(){
        return true;
    }

    public boolean disabled() {
        return this.disabled;
    }

    public void index(String index){
        this.index = index;
    }
    public boolean distributable(){
        return scope() != Distributable.LOCAL_SCOPE;
    }
    public String index(){
        return this.index;
    }
    public void disabled(boolean disabled) {
        this.disabled = disabled;
    }
    public long revision(){
        return this.revision;
    }
    public void revision(long revision){
        this.revision = revision;
    }

    public int routingNumber(){
        return this.routingNumber;
    }
    public void routingNumber(int routingNumber){
        this.routingNumber = routingNumber;
    }
    public int getFactoryId(){
        return -1;
    }
    public int getClassId(){
        return -1;
    }

    public long distributionId() {
        return distributionId;
    }
    @Override
    public void distributionId(long distributionId){
        this.distributionId = distributionId;
    }

    public String distributionKey() {
        return null;
    }
    @Override
    public void distributionKey(String distributionKey){

    }
    public int scope(){
        return Distributable.DATA_SCOPE;
    }
    public boolean onEdge(){
        return this.onEdge;
    }
    public void onEdge(boolean onEdge){
        this.onEdge = onEdge;
    }
    @Override
    public Key key(){
        return new SnowflakeKey(this.distributionId);
    }

    public boolean readKey(Recoverable.DataBuffer buffer){
        this.distributionId = buffer.readLong();
        return true;
    }
    public boolean writeKey(Recoverable.DataBuffer buffer){
        if(distributionId==0) return false;
        buffer.writeLong(distributionId);
        return true;
    }
    @Override
    public boolean equals(Object obj){
        Recoverable tc =(Recoverable) obj;
        return this.distributionId==(tc.distributionId());
    }
    @Override
    public int hashCode(){
        return Long.hashCode(distributionId);
    }
    @Override
    public String toString(){
        return (JsonUtil.toJsonString(this.toMap()));
    }

    @Override
    public JsonObject toJson(){
        return JsonUtil.toJsonObject(this.properties);
    }

}

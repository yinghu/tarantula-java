package com.icodesoftware.util;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;

import java.util.HashMap;
import java.util.Map;

public class RecoverableObject implements Recoverable {

    protected String bucket;

    protected String oid;
    protected String owner;
    protected  String label;

    protected  boolean disabled;

    protected  Map<String,Object> properties = new HashMap();

    protected  boolean onEdge;

    protected long timestamp;
    protected int version;

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

    public String oid(){
        return this.oid;
    }
    public void oid(String oid){
        this.oid = oid;
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
    public byte[] toBinary(){
        return JsonUtil.toJson(toMap());
    }
    public void fromBinary(byte[] payload){
        fromMap(JsonUtil.toMap(payload));
    }
    public boolean backup(){
        return false;
    }

    public boolean disabled() {
        return this.disabled;
    }

    public void index(String index){
        this.index = index;
    }
    public boolean distributable(){
        return true;
    }
    public String index(){
        return this.index;
    }
    public void disabled(boolean disabled) {
        this.disabled = disabled;
    }
    public int version(){
        return this.version;
    }
    public void version(int version){
        this.version = version;
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

    public String distributionKey() {
        if(this.bucket!=null&&this.oid!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public void distributionKey(String distributionKey){
        try{
            String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
            this.bucket = klist[0];
            this.oid = klist[1];
        }catch (Exception ex){
            //ignore wrong format key
        }
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
        return new DistributionKey(this.bucket,this.oid);
    }

    @Override
    public boolean equals(Object obj){
        Recoverable tc =(Recoverable) obj;
        return this.distributionKey().equals(tc.distributionKey());
    }
    @Override
    public int hashCode(){
        return this.distributionKey().hashCode();
    }
    @Override
    public String toString(){
        return (JsonUtil.toJsonString(this.toMap()));
    }

    public JsonObject toJson(){
        return JsonUtil.toJsonObject(this.properties);
    }
}

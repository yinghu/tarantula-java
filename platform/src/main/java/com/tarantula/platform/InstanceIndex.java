package com.tarantula.platform;

import com.tarantula.*;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.service.deployment.InstanceManager;

import java.util.List;
import java.util.Map;

/**
 * updated by yinghu on 3/6/2019.
 */
public class InstanceIndex  extends OnApplicationHeader implements InstanceRegistry{

    private boolean bank;
    private int capacity;
    private int count;
    private transient House house;
    private transient Statistics statistics;

    public TarantulaApplicationContext applicationContext;

    public InstanceManager application;

    public OnInstance.Listener onInstanceListener;

    public InstanceIndex(){
        this.vertex = "InstanceRegistry";
        this.label = "INS";
        this.onEdge = true;
    }

    public synchronized int count(int delta){
        return this.count=this.count+(delta);
    }

    public House house(){
        return this.house;
    }
    public void house(House house){
        this.house = house;
    }
    public Statistics statistics(){
        return this.statistics;
    }
    public void statistics(Statistics statistics){
        this.statistics = statistics;
    }
    public int capacity(){
        return this.capacity;
    }
    public void capacity(int capacity){
        this.capacity = capacity;
    }
    public void  bank(boolean bank){
        this.bank = bank;
    }
    public boolean  bank(){
        return this.bank;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableRegistry.INSTANCE_INDEX_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        //this.properties.put("applicationId",applicationId);
        this.properties.put("capacity",capacity);
        this.properties.put("bank",this.bank);
        this.properties.put("owner",owner!=null?owner:"n/a");
        this.properties.put("disabled",disabled);
        this.properties.put("accessMode",this.accessMode);
        this.properties.put("tournamentEnabled",this.tournamentEnabled);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        //this.applicationId = (String)properties.get("applicationId");
        this.capacity = ((Number)properties.get("capacity")).intValue();
        this.bank = (Boolean)properties.get("bank");
        this.owner =(String)properties.get("owner");
        this.disabled =(Boolean)properties.get("disabled");
        this.accessMode = ((Number)properties.get("accessMode")).intValue();
        this.tournamentEnabled =(Boolean)properties.get("tournamentEnabled");
    }
    @Override
    public String toString(){
        return "Instance Registry ["+this.owner+","+","+this.distributionKey()+","+this.accessMode+"] with capacity/count ["+this.capacity+","+this.count+"]tournament ["+tournamentEnabled+"]";
    }
    public synchronized int onJoin(Event event){
        int ret = application.onJoin(event,this.onInstanceListener);
        if(ret == InstanceRegistry.ON_INSTANCE){
            count++;
        }
        return ret;
    }
    public synchronized void onLeave(Session session){
        this.count -= this.applicationContext.onLeave(session);
        if(onInstanceListener!=null){
            this.onInstanceListener.onUpdated(new OnInstanceTrack(session.systemId(),session.stub(),this.applicationId,this.distributionKey(),false));
        }

    }

    public synchronized boolean transact(String systemId,double delta){
        OnInstance onInstance = this.applicationContext.onInstance(systemId);
        if(onInstance!=null&&onInstance.transact(delta)) {
            return true;
        }
        else{
            return false;
        }
    }

    public synchronized double balance(String systemId){
        OnInstance onInstance = this.applicationContext.onInstance(systemId);
        return onInstance!=null?onInstance.balance():0d;
    }
    public synchronized List<OnInstance> onInstance(){
        return this.applicationContext.onInstance();
    }
    public synchronized OnInstance onInstance(String systemId){
        return this.applicationContext.onInstance(systemId);
    }

    public void registerOnInstanceListener(OnInstance.Listener listener){
        this.onInstanceListener = listener;
    }

    @Override
    public String distributionKey() {
        if(this.bucket!=null&&this.oid!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(oid).append(Recoverable.PATH_SEPARATOR).append(routingNumber).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public void distributionKey(String distributionKey) {
        String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
        this.bucket = klist[0];
        this.oid = klist[1];
        this.routingNumber = Integer.parseInt(klist[2]);
    }
    @Override
    public Key key(){
        return new IndexKey(this.bucket,this.oid,this.routingNumber);
    }
    @Override
    public synchronized void disabled(boolean disabled) {
        this.disabled = disabled;
    }
    @Override
    public int hashCode(){
        return this.distributionKey().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        InstanceIndex ix = (InstanceIndex)obj;
        return this.distributionKey().equals(ix.distributionKey());
    }
}

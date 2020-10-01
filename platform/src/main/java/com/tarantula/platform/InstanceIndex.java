package com.tarantula.platform;

import com.icodesoftware.*;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.service.deployment.InstanceManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * updated by yinghu on 7/11/2020
 */
public class InstanceIndex  extends OnApplicationHeader implements InstanceRegistry {

    private int capacity;
    private int count;

    public TarantulaApplicationContext applicationContext;

    public InstanceManager application;

    public List<OnInstance.Listener> onInstanceListener = new CopyOnWriteArrayList<>();

    public InstanceIndex(){
        this.label = LABEL;
        this.onEdge = true;
    }

    public synchronized int count(int delta){
        return this.count=this.count+(delta);
    }

    public int capacity(){
        return this.capacity;
    }
    public void capacity(int capacity){
        this.capacity = capacity;
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
        this.properties.put("capacity",capacity);
        this.properties.put("owner",owner!=null?owner:"n/a");
        this.properties.put("disabled",disabled);
        this.properties.put("accessMode",this.accessMode);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.capacity = ((Number)properties.get("capacity")).intValue();
        this.owner =(String)properties.get("owner");
        this.disabled =(Boolean)properties.get("disabled");
        this.accessMode = ((Number)properties.get("accessMode")).intValue();

    }
    @Override
    public String toString(){
        return "Instance Registry ["+this.owner+","+","+this.distributionKey()+","+this.accessMode+"] with capacity/count ["+this.capacity+","+this.count+"]";
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
        this.onInstanceListener.forEach((l)->{
            l.onUpdated(new OnInstanceTrack(session.systemId(),session.stub(),this.applicationId,this.distributionKey(),false));
        });

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
        this.onInstanceListener.add(listener);
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

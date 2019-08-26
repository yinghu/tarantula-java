package com.tarantula.platform;
import com.tarantula.*;
import com.tarantula.platform.service.cluster.PortableRegistry;
import java.util.Map;
/**
 * Updated by yinghu on 8/23/19
 */
public class HouseTrack extends OnApplicationHeader implements House {

    private boolean bank;

    public HouseTrack(){
        this.vertex = "House";
        this.label = "IRH";
    }
    public void  bank(boolean bank){
        this.bank = bank;
    }
    public boolean  bank(){
        return bank;
    }

    public boolean bankrupt() {
        return bank?false:(balance<0);
    }


    public int getFactoryId() {
        return PortableRegistry.OID;
    }


    public int getClassId() {
        return PortableRegistry.HOUSE_CID;
    }


    @Override
    public String toString(){
        return "House ["+this.oid+"]"+systemId+"/"+owner+"/"+balance+"/"+bank+"]";
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("balance",this.balance);
        this.properties.put("bank",this.bank);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.balance = ((Number)properties.get("balance")).doubleValue();
        this.bank =(boolean)properties.get("bank");
    }
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }


    public synchronized boolean transact(double delta) {
        this.balance = this.balance+(delta);
        this.dataStore.update(this);
        return true;
    }
    public void dataStore(DataStore dataStore){
        this.dataStore = dataStore;
    }
    public void update(){
        this.dataStore.update(this);
    }
}

package com.tarantula.platform;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.*;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.io.IOException;
import java.util.Map;

/**
 * Updated by yinghu on 6/8/2018.
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
    public void writePortable(PortableWriter out) throws IOException {
        out.writeUTF("o",this.oid);
        out.writeDouble("3", this.balance);
        out.writeBoolean("4",this.bank);

    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        this.oid = in.readUTF("o");
        this.balance = in.readDouble("3");
        this.bank = in.readBoolean("4");

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
    public void onUpdate(){
        this.dataStore.update(this);
    }
}

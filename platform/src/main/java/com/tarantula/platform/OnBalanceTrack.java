package com.tarantula.platform;

import com.tarantula.OnBalance;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

/**
 * Updated by yinghu on 4/12/2019.
 *
 */
public class OnBalanceTrack extends OnApplicationHeader implements OnBalance {

    public OnBalanceTrack(){
        this.vertex = "OnBalance";
    }
    public OnBalanceTrack(String systemId, double balance){
        this();
        this.owner = systemId;
        this.balance=balance;
        redeemed = true;
    }

    public String toString(){
        return "OnBalance ["+owner+"/"+balance+"/"+event+"/"+redeemed+"]";
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("owner",this.owner);
        this.properties.put("balance",this.balance);
        this.properties.put("redeemed",this.redeemed);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.owner = (String)properties.get("owner");
        this.balance = ((Number)properties.get("balance")).doubleValue();
        this.redeemed =(boolean)properties.get("redeemed");
    }

    @Override
    public void distributionKey(String distributionKey) {
        //no key
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    public int getClassId() {
        return PresencePortableRegistry.ON_BALANCE_CID;
    }
}

package com.tarantula.service.payment;

import com.tarantula.platform.RecoverableObject;

import java.util.Map;

/**
 * Created by yinghu lu on 2/15/2019.
 */
public class VirtualCreditsPack extends RecoverableObject {

    public String name;
    public double price;
    public double credits;

    public VirtualCreditsPack(){}

    public VirtualCreditsPack(String name,double price,double credits){
        this.name = name;
        this.price = price;
        this.credits = credits;
    }
    @Override
    public int getFactoryId() {
        return MarketplacePortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return MarketplacePortableRegistry.VIRTUAL_CREDITS_PACK;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("name",this.name);
        this.properties.put("price",this.price);
        this.properties.put("credits",this.credits);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String)properties.get("name");
        this.price = ((Number)properties.get("price")).doubleValue();
        this.credits =((Number)properties.get("credits")).doubleValue();
    }

}

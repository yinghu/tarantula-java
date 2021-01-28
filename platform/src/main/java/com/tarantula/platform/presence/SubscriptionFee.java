package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.tarantula.platform.IndexKey;

import java.util.Map;
import com.icodesoftware.util.RecoverableObject;
public class SubscriptionFee extends RecoverableObject {

    public static final String DataStore = "purchase";

    public String type;
    public String name;
    public String amount;
    public String currency;
    public int durationMonths;
    public SubscriptionFee(){

    }
    public SubscriptionFee(String type,String name,String amount,String currency,int durationMonths){
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.currency =currency;
        this.durationMonths = durationMonths;
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    public int getClassId() {
        return PresencePortableRegistry.PURCHASE_CID;
    }
    public Map<String,Object> toMap(){
        properties.put("1",type);
        properties.put("2",name);
        properties.put("3",amount);
        properties.put("4",currency);
        properties.put("5",durationMonths);
        properties.put("6",timestamp);
        return properties;
    }
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("1");
        this.name = (String)properties.get("2");
        this.amount = (String)properties.get("3");
        this.currency = (String)properties.get("4");
        this.durationMonths = ((Number) properties.get("5")).intValue();
        this.timestamp = ((Number) properties.get("6")).longValue();
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
    public SubscriptionFee log(String systemId,int count){
        SubscriptionFee _log = new SubscriptionFee(this.type,this.name,this.amount,this.currency,this.durationMonths);
        String[] klist = systemId.split(Recoverable.PATH_SEPARATOR);
        _log.bucket(klist[0]);
        _log.oid(this.oid = klist[1]);
        _log.routingNumber(count);
        return _log;
    }
}

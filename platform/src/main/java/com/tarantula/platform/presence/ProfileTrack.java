package com.tarantula.platform.presence;

import com.tarantula.Profile;

import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Updated by yinghu lu on 8/26/19
 */
public class ProfileTrack extends RecoverableObject implements Profile {


    private String nickname;
    private String avatar;
    private String emailAddress;
    public ProfileTrack(){
        this.vertex = "Profile";
    }
    public ProfileTrack(String bucket,String oid){
        this();
        this.bucket = bucket;
        this.oid = oid;
    }

    public String nickname() {
        return this.nickname;
    }

    public void nickname(String nickname) {
        this.nickname = nickname;
    }
    public void avatar(String avatar){
        this.avatar = avatar;
    }
    public String avatar(){
        return  this.avatar;
    }
    public void emailAddress(String emailAddress){
        this.emailAddress = emailAddress;
    }
    public String emailAddress(){
        return this.emailAddress;
    }

    public void header(String header,String value){
        this.properties.put(header,value);
    }
    public String header(String header){
        return (String) this.properties.get(header);
    }

    public Map<String,String> header(){
        HashMap<String,String> hm = new HashMap<>();
        properties.forEach((k,v)->hm.put(k,(String)v));
        return hm;
    }

    @Override
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return UserPortableRegistry.PROFILE_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("emailAddress",this.emailAddress!=null?this.emailAddress:"n/a");
        this.properties.put("avatar",this.avatar!=null?this.avatar:"n/a");
        this.properties.put("nickname",this.nickname!=null?this.nickname:"n/a");
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.emailAddress = (String)properties.get("emailAddress");
        this.avatar = (String)properties.get("avatar");
        this.nickname = (String)properties.get("nickname");
    }

    @Override
    public String toString(){
        return "["+nickname+"/"+emailAddress+"]";
    }

    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}

package com.tarantula.platform.presence;

import com.tarantula.Content;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class Avatar extends RecoverableObject implements Content {

    private String type;
    private String name;

    public Avatar(){
        this.vertex = "Avatar";
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("type",type);
        this.properties.put("name",name);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.type = (String)properties.get("type");
        this.name = (String)properties.get("name");
    }
    @Override
    public int getFactoryId() {
        return UserPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return UserPortableRegistry.AVATAR_CID;
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public void type(String type) {
        this.type = type;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }
}

package com.tarantula.platform.achievement;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class AchievementProgress extends RecoverableObject {

    public AchievementProgress(){

    }
    public AchievementProgress(String name){
        this.label = name;
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_PROGRESS_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){

    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
}

package com.tarantula.game;

import com.tarantula.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Rating extends RecoverableObject {

    public int rank =1; //rank of zone
    public int level=1; //level of arena
    public double xp=1; //xp of arena

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("rank",rank);
        this.properties.put("level",level);
        this.properties.put("xp",xp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("rank")).intValue();
        this.level =((Number)properties.get("level")).intValue();
        this.xp = ((Number)properties.get("xp")).doubleValue();
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.RATING_CID;
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}

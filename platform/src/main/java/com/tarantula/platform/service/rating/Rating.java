package com.tarantula.platform.service.rating;

import com.tarantula.Recoverable;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
/**
 * Created by yinghu lu on 4/14/2020.
 * Key form [systemId]/Rating
 */
public class Rating extends RecoverableObject {

    public int rank =1; //rank of zone
    public int level=1; //level of arena
    public double zxp=10;  //xp of zone
    public double xp=100; //total xp
    public double elo = 1200; //elo rating
    public int csw = 0; //consecutive winnings
    public Rating(){
        this.vertex = "Rating";
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("rank",rank);
        this.properties.put("level",level);
        this.properties.put("zxp",zxp);
        this.properties.put("xp",xp);
        this.properties.put("elo",elo);
        this.properties.put("csw",csw);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("rank")).intValue();
        this.level =((Number)properties.get("level")).intValue();
        this.zxp = ((Number)properties.get("zxp")).doubleValue();
        this.xp = ((Number)properties.get("xp")).doubleValue();
        this.elo = ((Number)properties.get("elo")).doubleValue();
        this.csw =((Number)properties.get("csw")).intValue();
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

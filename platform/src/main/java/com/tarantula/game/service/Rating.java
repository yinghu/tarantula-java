package com.tarantula.game.service;

import com.tarantula.Recoverable;
import com.tarantula.Updatable;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.Stub;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
/**
 * Created by yinghu lu on 4/14/2020.
 * Key form [systemId]/Rating
 */
public class Rating extends RecoverableObject implements Updatable {

    public static double BASE_POINTS = 100;

    public int rank =1; //rank of zone
    public int level=1; //level of arena
    public double lxp=0;  //xp of level
    public double xp=0; //total xp
    public double elo = 1200; //elo service
    public int csw = 0; //consecutive winnings


    public Rating(){
        this.vertex = "Rating";
    }

    public void update(Stub stub){
        double dxp = (1/stub.rank+stub.pxp/BASE_POINTS)*BASE_POINTS;
        if(stub.rank==1){
            csw++;
            dxp = dxp+(csw-1)*BASE_POINTS;
        }
        else{
            csw=0;
        }
        lxp += dxp;
        xp += dxp;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("rank",rank);
        this.properties.put("level",level);
        this.properties.put("lxp",lxp);
        this.properties.put("xp",xp);
        this.properties.put("elo",elo);
        this.properties.put("csw",csw);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("rank")).intValue();
        this.level =((Number)properties.get("level")).intValue();
        this.lxp = ((Number)properties.get("lxp")).doubleValue();
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

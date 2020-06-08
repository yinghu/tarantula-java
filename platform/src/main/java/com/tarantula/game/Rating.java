package com.tarantula.game;

import com.tarantula.DataStore;
import com.tarantula.Recoverable;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.Stub;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
/**
 * Updated by yinghu lu on 6/8/2020.
 * Key form [systemId]/Rating
 */
public class Rating extends RecoverableObject implements DataStore.Updatable {

    public static double BASE_POINTS = 100;
    public static int LEVEL_UP_BASE = 1000;
    public static int RANK_UP_BASE = 10;
    public int rank =1; //rank of zone
    public int level = 1; //total level
    public int xpLevel=1; //level of arena
    public double lxp=LEVEL_UP_BASE;  //xp of level
    public double xp=0; //total xp
    public double elo = 1200; //elo service
    public int csw = 0; //consecutive winnings


    public Rating(){
        this.vertex = "Rating";
    }

    public void update(Stub stub){
        if(stub.rank==0){
            return;
        }
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
        xpLevel = (int)lxp/LEVEL_UP_BASE;
        if(xpLevel%RANK_UP_BASE==0){//rank up
            rank++;
            //reset next level from 1 - 10 and rank up again
            xpLevel = 1;
            lxp = LEVEL_UP_BASE;
        }
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",rank);
        this.properties.put("2",level);
        this.properties.put("3",xpLevel);
        this.properties.put("4",lxp);
        this.properties.put("5",xp);
        this.properties.put("6",elo);
        this.properties.put("7",csw);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("1")).intValue();
        this.level =((Number)properties.get("2")).intValue();
        this.xpLevel =((Number)properties.get("3")).intValue();
        this.lxp = ((Number)properties.get("4")).doubleValue();
        this.xp = ((Number)properties.get("5")).doubleValue();
        this.elo = ((Number)properties.get("6")).doubleValue();
        this.csw =((Number)properties.get("7")).intValue();
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

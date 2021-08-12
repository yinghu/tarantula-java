package com.tarantula.game;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class Rating extends RecoverableObject implements DataStore.Updatable, Portable {

    public static double BASE_POINTS = 100;

    public int rank =1; //rank of lobby
    public int level = 1; //total level
    public double xp =0; //total xp
    public int arenaLevel =1; //level of arena
    public double arenaXp =0;  //xp of arena level

    public double elo = 1200; //elo service
    public int csw = 0; //consecutive winnings


    public Rating(){
        this.label = "Rating";
    }

    public void update(Stub stub){
        if(stub.rank==0) return;
        double dxp = (1/stub.rank+stub.pxp/BASE_POINTS)*BASE_POINTS;
        if(stub.rank==1){
            csw++;
            dxp = dxp+(csw-1)*BASE_POINTS;
        }
        else{
            csw=0;
        }
        arenaXp += dxp;
        xp += dxp;
        int _xpLevel = (int)arenaXp/stub.levelUpBase;
        if(_xpLevel>arenaLevel){
            _xpLevel = _xpLevel-arenaLevel;//xplevel delta
            arenaLevel = arenaLevel +(_xpLevel);//
            level = level+(_xpLevel);//add level jump delta
        }
        if(arenaLevel-stub.rankUpBase>0){//rank up if level pass the base
            rank++;
            //reset next level from 1 - 10 and rank up again
            arenaLevel = 1;
            arenaXp = 0;
        }
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",rank);
        this.properties.put("2",level);
        this.properties.put("3",arenaLevel);
        this.properties.put("4",arenaXp);
        this.properties.put("5",xp);
        this.properties.put("6",elo);
        this.properties.put("7",csw);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("1")).intValue();
        this.level =((Number)properties.get("2")).intValue();
        this.arenaLevel =((Number)properties.get("3")).intValue();
        this.arenaXp = ((Number)properties.get("4")).doubleValue();
        this.xp = ((Number)properties.get("5")).doubleValue();
        this.elo = ((Number)properties.get("6")).doubleValue();
        this.csw =((Number)properties.get("7")).intValue();
    }

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.RATING_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeUTF("1",owner);
        //portableWriter.writeInt("rank",rank);
        //portableWriter.writeInt("xpLevel",xpLevel);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        owner = portableReader.readUTF("1");
        //this.rank = portableReader.readInt("rank");
        //this.xpLevel = portableReader.readInt("xpLevel");
    }

    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

}

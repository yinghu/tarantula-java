package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class Rating extends PlayerGameObject implements DataStore.Updatable, Portable {

    public static double BASE_POINTS = 100;
    public static int ARENA_LEVEL_LIMIT = 10;
    public static int RANK_UP_LEVEL_BASE = 100;

    public int rank =1; //rank of lobby
    public int level = 1; //total level
    public double xp =0; //total xp
    public int arenaLevel =1; //level of arena
    public double arenaXp =0;  //xp of arena level


    public Rating(){
        this.label = "Rating";
    }

    public Rating update(double xpDelta,double arenaXpLimit){
        double dxp = (xpDelta/BASE_POINTS)*BASE_POINTS;
        arenaXp += dxp;
        xp += dxp;
        if(arenaXp < arenaXpLimit) return this;
        //level up
        arenaLevel = arenaLevel==ARENA_LEVEL_LIMIT?1:(arenaLevel+1);
        level++;
        arenaXp = 0;
        int _tryRank = 1+((level-1)/RANK_UP_LEVEL_BASE);
        if(_tryRank>rank) rank = _tryRank;
        return this;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",rank);
        this.properties.put("2",level);
        this.properties.put("3",arenaLevel);
        this.properties.put("4",arenaXp);
        this.properties.put("5",xp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("1")).intValue();
        this.level =((Number)properties.get("2")).intValue();
        this.arenaLevel =((Number)properties.get("3")).intValue();
        this.arenaXp = ((Number)properties.get("4")).doubleValue();
        this.xp = ((Number)properties.get("5")).doubleValue();
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
        portableWriter.writeUTF("1",bucket);
        portableWriter.writeUTF("2",oid);
        portableWriter.writeInt("3",arenaLevel);
        portableWriter.writeDouble("4",arenaXp);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        bucket = portableReader.readUTF("1");
        oid = portableReader.readUTF("2");
        arenaLevel = portableReader.readInt("3");
        arenaXp = portableReader.readDouble("4");
    }

    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Rank",rank);
        jsonObject.addProperty("Level",level);
        jsonObject.addProperty("Xp",xp);
        jsonObject.addProperty("ArenaLevel",arenaLevel);
        jsonObject.addProperty("ArenaXP",arenaXp);
        return jsonObject;
    }
}

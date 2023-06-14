package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;
import java.util.Map;

public class Rating extends PlayerGameObject implements DataStore.Updatable {


    public static int RANK_UP_LEVEL_BASE = 100;

    public int rank = 1; //rank of lobby
    public int level = 1; //total level
    public double xp =0; //total xp
    public boolean granted;
    private double levelUpXp =0;  //xp of arena level

    public Rating(){
        this.label = "Rating";
    }

    public Rating update(double xpDelta,double levelUpLimit){
        levelUpXp += xpDelta;
        xp += xpDelta;
        if(levelUpXp < levelUpLimit) return this;
        //level up
        level++;
        levelUpXp = 0;
        int _tryRank = 1+((level-1)/RANK_UP_LEVEL_BASE);
        if(_tryRank > rank) rank = _tryRank;
        return this;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",rank);
        this.properties.put("2",level);
        this.properties.put("3",levelUpXp);
        this.properties.put("4",xp);
        this.properties.put("5",granted);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.rank = ((Number)properties.get("1")).intValue();
        this.level =((Number)properties.get("2")).intValue();
        this.levelUpXp = ((Number)properties.get("3")).doubleValue();
        this.xp = ((Number)properties.get("4")).doubleValue();
        this.granted = ((boolean)properties.getOrDefault("5",false));
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
        portableWriter.writeInt("3",rank);
        portableWriter.writeInt("4",level);
        portableWriter.writeDouble("5",levelUpXp);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        bucket = portableReader.readUTF("1");
        oid = portableReader.readUTF("2");
        rank = portableReader.readInt("3");
        level = portableReader.readInt("4");
        levelUpXp = portableReader.readDouble("5");
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
        return jsonObject;
    }
}

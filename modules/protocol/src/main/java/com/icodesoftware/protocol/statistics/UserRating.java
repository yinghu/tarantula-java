package com.icodesoftware.protocol.statistics;

import com.google.gson.JsonObject;

import com.icodesoftware.Rating;
import com.icodesoftware.protocol.ProtocolPortableRegistry;
import com.icodesoftware.util.RecoverableObject;

public class UserRating extends RecoverableObject implements Rating {


    public static int RANK_UP_LEVEL_BASE = 100;

    public int rank = 1; //rank of lobby
    public int level = 1; //total level
    public double xp =0; //total xp
    public boolean granted;
    private double levelUpXp =0;  //xp of arena level

    public UserRating(){
        this.onEdge = true;
        this.label = "rating";
    }

    public Rating update(double xpDelta, double levelUpLimit){
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
    public Rating update(double xpDelta,Listener listener) {
        levelUpXp += xpDelta;
        xp += xpDelta;
        if(!listener.levelUp(levelUpXp)) return this;
        level++;
        levelUpXp = 0;
        if(!listener.rankUp(level)) return this;
        rank++;
        return this;
    }

    public boolean read(DataBuffer buffer){
        this.rank = buffer.readInt();
        this.level = buffer.readInt();
        this.levelUpXp = buffer.readDouble();
        this.xp = buffer.readDouble();
        this.granted = buffer.readBoolean();
        return true;
    }
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(rank);
        buffer.writeInt(level);
        buffer.writeDouble(levelUpXp);
        buffer.writeDouble(xp);
        buffer.writeBoolean(granted);
        return true;
    }

    @Override
    public int getFactoryId() {
        return ProtocolPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ProtocolPortableRegistry.USER_RATION_CID;
    }



    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Rank",rank);
        jsonObject.addProperty("Level",level);
        jsonObject.addProperty("Xp",xp);
        return jsonObject;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public double xp() {
        return xp;
    }
}

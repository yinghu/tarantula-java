package com.tarantula.game;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Rating;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class GameRating extends PlayerGameObject implements Rating {


    public static int RANK_UP_LEVEL_BASE = 100;

    public int rank = 1; //rank of lobby
    public int level = 1; //total level
    public double xp =0; //total xp
    public boolean granted;
    private double levelUpXp =0;  //xp of arena level

    public GameRating(){
        this.onEdge = true;
        this.label = "rating";
    }

    public Rating elo(boolean win,long opponentId,long teamPower){
        //no direct call
        return this;
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
        return PortableEventRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PortableEventRegistry.RATING_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        //portableWriter.writeUTF("1",bucket);
        portableWriter.writeLong("2",distributionId);
        portableWriter.writeInt("3",rank);
        portableWriter.writeInt("4",level);
        portableWriter.writeDouble("5",levelUpXp);
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        //bucket = portableReader.readUTF("1");
        distributionId = portableReader.readLong("2");
        rank = portableReader.readInt("3");
        level = portableReader.readInt("4");
        levelUpXp = portableReader.readDouble("5");
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

    public void level(int eloAssigned){
        this.level = eloAssigned;
    }

    public static Rating from(long distributionId,int level){
        GameRating rating = new GameRating();
        rating.distributionId(distributionId);
        rating.level(level);
        return rating;
    }
}

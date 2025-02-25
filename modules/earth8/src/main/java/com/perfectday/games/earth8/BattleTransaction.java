package com.perfectday.games.earth8;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

public class BattleTransaction extends RecoverableObject {

    public long chapterId;
    public long stageId;

    public long[] party;

    public boolean win;

    public String TEMP_BattleStage;

    public long seasonId;
    public long opponentId;
    public long teamId; //the player's team id on battle start

    //Data store write contract
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(chapterId);
        buffer.writeLong(stageId);
        buffer.writeInt(party.length);
        for(long unit : party){
            buffer.writeLong(unit);
        }
        buffer.writeBoolean(win);
        buffer.writeBoolean(disabled);
        buffer.writeUTF8(TEMP_BattleStage);
        try{
            buffer.writeLong(seasonId);
            buffer.writeLong(opponentId);
            buffer.writeLong(teamId);
        }catch (Exception ex){
            //ignore
        }
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        chapterId = buffer.readLong();
        stageId = buffer.readLong();
        int size = buffer.readInt();
        party = new long[size];
        for(int i=0;i<size;i++){
            party[i]=buffer.readLong();
        }
        win = buffer.readBoolean();
        disabled = buffer.readBoolean();
        TEMP_BattleStage = buffer.readUTF8();
        try{
            seasonId = buffer.readLong();
            opponentId = buffer.readLong();
            teamId = buffer.readLong();
        }catch (Exception exception){
            //ignore
        }
        return true;
    }

    //LIMIT RESPONSE SIZE FROM DEFAULT JSON-CONVERT
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("BattleId",distributionKey());
        jsonObject.addProperty("SeasonId",seasonId);
        jsonObject.addProperty("OpponentId",Long.toString(opponentId));
        jsonObject.addProperty("TeamId",Long.toString(teamId));
        return jsonObject;
    }

    //data validate contract
    @Override
    public boolean validate() {
        for(long unit : party){
            if(unit < 0) return false;
        }
        return true;
    }

    public static BattleTransaction fromJson(byte[] payload){
        JsonObject json = JsonUtil.parse(payload);
        BattleTransaction self = new BattleTransaction();
        self.distributionId(JsonUtil.getJsonLong(json,"BattleId",0));
        self.win = JsonUtil.getJsonBool(json,"Win",false);
        self.chapterId = JsonUtil.getJsonLong(json,"ChapterId",0);
        self.stageId = JsonUtil.getJsonLong(json,"StageId",0);
        self.seasonId = JsonUtil.getJsonLong(json,"SeasonId",0);
        self.opponentId = JsonUtil.getJsonLong(json,"OpponentId",0);
        self.teamId = JsonUtil.getJsonLong(json,"TeamId",0);
        JsonArray party = JsonUtil.getJsonArray(json, "Party");
        self.party = new long[party.size()];
        for(int i=0;i<self.party.length;i++){
            self.party[i]=party.get(i).getAsLong();
        }

        self.TEMP_BattleStage = JsonUtil.getJsonString(json, "TEMP_StageName", "");
        return self;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.BATTLE_TRANSACTION_CID;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }
}

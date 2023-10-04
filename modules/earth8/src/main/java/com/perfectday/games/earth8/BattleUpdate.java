package com.perfectday.games.earth8;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.HashMap;

public class BattleUpdate extends RecoverableObject {

    public enum UpdateId {UnitXpUP,UnitRankUp,UnitSkillUp}

    public UpdateId updateId;
    public long unitId;
    public long equipmentId;

    public HashMap<String,Integer> currency;

    //public int xpGain;
    //public int rank;


    //Data store write contract
    @Override
    public boolean write(DataBuffer buffer) {
        //buffer.writeLong(chapterId);
        //buffer.writeLong(stageId);
        //buffer.writeInt(party.length);
        //for(long unit : party){
            //buffer.writeLong(unit);
        //}
        //buffer.writeBoolean(win);
        buffer.writeBoolean(disabled);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        //chapterId = buffer.readLong();
        //stageId = buffer.readLong();
        int size = buffer.readInt();
        //party = new long[size];
        for(int i=0;i<size;i++){
            //party[i]=buffer.readLong();
        }
        //win = buffer.readBoolean();
        disabled = buffer.readBoolean();
        return true;
    }

    //LIMIT RESPONSE SIZE FROM DEFAULT JSON-CONVERT
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("BattleId",distributionId);
        return jsonObject;
    }

    //data validate contract
    @Override
    public boolean validate() {
        //for(long unit : party){
            //if(unit <= 0) return false;
        //}
        //return chapterId >0 && stageId > 0;
        return true;
    }


    public static BattleUpdate fromJson(byte[] payload){
        JsonObject json = JsonUtil.parse(payload);
        BattleUpdate battleTransaction = new BattleUpdate();
        //battleTransaction.chapterId = json.get("ChapterId").getAsLong();
        //battleTransaction.stageId = json.get("StageId").getAsLong();
        JsonArray party = json.get("Party").getAsJsonArray();
        //battleTransaction.party = new long[party.size()];
        //for(int i=0;i<battleTransaction.party.length;i++){
            //battleTransaction.party[i]=party.get(i).getAsLong();
        //}
        return battleTransaction;
    }

}

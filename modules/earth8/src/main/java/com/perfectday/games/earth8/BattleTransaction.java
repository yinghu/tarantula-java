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
        return true;
    }

    //LIMIT RESPONSE SIZE FROM DEFAULT JSON-CONVERT
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("BattleId",distributionKey());
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
        self.chapterId = json.get("ChapterId").getAsLong();
        self.stageId = json.get("StageId").getAsLong();
        JsonArray party = json.get("Party").getAsJsonArray();
        self.party = new long[party.size()];
        for(int i=0;i<self.party.length;i++){
            self.party[i]=party.get(i).getAsLong();
        }

        self.TEMP_BattleStage = BattleUpdate.GetJsonString(json, "TEMP_StageName", "");

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

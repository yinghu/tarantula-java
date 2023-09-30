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

    public boolean finished;
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(chapterId);
        buffer.writeLong(stageId);
        buffer.writeInt(party.length);
        for(long unit : party){
            buffer.writeLong(unit);
        }
        buffer.writeBoolean(win);
        buffer.writeBoolean(finished);
        return true;
    }

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
        finished = buffer.readBoolean();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("BattleId",distributionId);
        return jsonObject;
    }

    @Override
    public boolean validate() {
        for(long unit : party){
            if(unit <= 0) return false;
        }
        return chapterId >0 && stageId > 0;
    }

    public static BattleTransaction fromJson(byte[] payload){
        JsonObject json = JsonUtil.parse(payload);
        BattleTransaction battleTransaction = new BattleTransaction();
        battleTransaction.chapterId = json.get("ChapterId").getAsLong();
        battleTransaction.stageId = json.get("StageId").getAsLong();
        JsonArray party = json.get("Party").getAsJsonArray();
        battleTransaction.party = new long[party.size()];
        for(int i=0;i<battleTransaction.party.length;i++){
            battleTransaction.party[i]=party.get(i).getAsLong();
        }
        return battleTransaction;
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

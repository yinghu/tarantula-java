package com.perfectday.games.earth8.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.perfectday.games.earth8.BattleTransaction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BattleTransactionTest {

    @Test(groups = { "BattleTransaction" })
    public void parsePayloadTest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ChapterId",100);
        jsonObject.addProperty("StageId",200);
        JsonArray party = new JsonArray();
        party.add(1000);
        party.add(2000);
        party.add(3000);
        jsonObject.add("Party",party);
        BattleTransaction battleTransaction = BattleTransaction.fromJson(jsonObject.toString().getBytes());
        Assert.assertEquals(battleTransaction.chapterId,100);
        Assert.assertEquals(battleTransaction.stageId,200);
        Assert.assertEquals(battleTransaction.party[0],1000);
        Assert.assertEquals(battleTransaction.party[1],2000);
        Assert.assertEquals(battleTransaction.party[2],3000);
        Assert.assertTrue(battleTransaction.validate());

        battleTransaction.distributionId(5000);
        JsonObject response = battleTransaction.toJson();
        Assert.assertEquals(response.get("BattleId").getAsLong(),5000);
        Assert.assertEquals(response.get("Successful").getAsBoolean(),true);

    }

}

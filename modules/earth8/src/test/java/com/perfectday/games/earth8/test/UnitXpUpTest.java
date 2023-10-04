package com.perfectday.games.earth8.test;

import com.google.gson.JsonObject;
import com.perfectday.games.earth8.BattleUpdate;
import com.perfectday.games.earth8.UnitXpUp;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UnitXpUpTest {

    @Test(groups = { "BattleTransaction" })
    public void parsePayloadTest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("UpdateId", BattleUpdate.UpdateId.UnitXpUP.ordinal());
        jsonObject.addProperty("UnitId",100);
        jsonObject.addProperty("EquipmentId",200);
        JsonObject currencies = new JsonObject();
        currencies.addProperty("Gem",10);
        currencies.addProperty("XPPortionA",5);
        currencies.addProperty("XPPortionB",3);
        jsonObject.add("Currencies",currencies);
        jsonObject.addProperty("XpGain",10);

        UnitXpUp unitXpUp = (UnitXpUp) BattleUpdate.fromJson(jsonObject.toString().getBytes());
        Assert.assertEquals(unitXpUp.updateId, BattleUpdate.UpdateId.UnitXpUP);
        Assert.assertEquals(unitXpUp.unitId,100);
        Assert.assertEquals(unitXpUp.equipmentId,200);
        Assert.assertEquals(unitXpUp.xpGain,10);
        Assert.assertEquals(unitXpUp.currencies.size(),3);
        Assert.assertEquals(unitXpUp.currencies.get("Gem"),10);
        Assert.assertEquals(unitXpUp.currencies.get("XPPortionA"),5);
        Assert.assertEquals(unitXpUp.currencies.get("XPPortionB"),3);
    }

}

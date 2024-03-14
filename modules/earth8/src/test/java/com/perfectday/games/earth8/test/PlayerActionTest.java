package com.perfectday.games.earth8.test;

import com.google.gson.JsonObject;
import com.perfectday.games.earth8.inbox.PlayerAction;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PlayerActionTest {

    @Test(groups = { "PlayerAction" })
    public void playerActionTest() {
        PlayerAction playerAction = new PlayerAction();
        playerAction.name("ShippingFormCompleted");
        playerAction.completed=(true);
        Assert.assertEquals(playerAction.name(),"ShippingFormCompleted");
        Assert.assertEquals(playerAction.completed,true);
        JsonObject expected = new JsonObject();
        expected.addProperty("Name","ShippingFormCompleted");
        expected.addProperty("Completed",true);
        Assert.assertEquals(playerAction.toJson().toString(),expected.toString());
    }
}

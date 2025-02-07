package com.tarantula.game.module;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PVPMockData {
    public static String getSeasonInfo(){
        return "{\"SeasonId\":672744233026727936,\"EndTime\":\"2025-02-05T13:15:50.293152\",\"Faction1\":0,\"Faction2\":0,\"Faction3\":0}";
    }

    public static String getPVPList(){
        JsonObject response = new JsonObject();

        JsonObject playerData = new JsonObject();
        playerData.addProperty("elo", 400);
        playerData.addProperty("teamPower", 40);
        playerData.addProperty("formation", "formation");

        JsonArray opponents = new JsonArray();
        JsonObject opponent = new JsonObject();
        opponent.addProperty("playerId", "345346733");
        opponent.add("playerData", playerData);
        opponent.addProperty("battleResult", "1");
        opponent.addProperty("points", "5");
        opponents.add(opponent);
        opponents.add(opponent);

        response.add("playerData", playerData);
        response.addProperty("nextRefreshTime", "2025-02-05T13:15:50.293152");
        response.add("opponents", opponents);

        return response.toString();
    }

    public static String getBattleLog(){
        JsonObject response = new JsonObject();

        JsonArray defense = new JsonArray();
        JsonArray attack = new JsonArray();

        JsonObject playerData = new JsonObject();
        playerData.addProperty("elo", 400);
        playerData.addProperty("teamPower", 40);
        playerData.addProperty("formation", "formation");

        JsonObject attackBattle = new JsonObject();
        attackBattle.addProperty("playerId", "345346733");
        attackBattle.add("playerData", playerData);
        attackBattle.addProperty("battleResult", "1");
        attackBattle.addProperty("points", "5");

        JsonObject defenseBattle = attackBattle.deepCopy();
        defenseBattle.addProperty("timeStamp", "2025-02-05T13:15:50.293152");

        defense.add(defenseBattle);
        defense.add(defenseBattle);
        attack.add(attackBattle);
        attack.add(attackBattle);

        response.add("defense", defense);
        response.add("attack", attack);

        return response.toString();
    }
}

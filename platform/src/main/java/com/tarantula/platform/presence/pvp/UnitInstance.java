package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;

public class UnitInstance extends RecoverableObject {

    public int level;
    public int rank;
    public int currentLevelExperience;
    public int currentRankExperience;
    public String configID;

    public long weaponIDValue;
    public long helmetIDValue;
    public long chestPieceIDValue;
    public long glovesIDValue;
    public long forceFieldIDValue;
    public long bootsIDValue;

    public int[] abilityRanks; // 0-4
    public int passiveRank;


}

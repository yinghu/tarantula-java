package com.tarantula.platform.presence.pvp;

import com.icodesoftware.util.RecoverableObject;

public class EquipmentInstance extends RecoverableObject {

    public int level;
    public int rank;
    public int currentLevelExperience;
    public int currentRankExperience;

    public EquipmentStat primaryStat;
    public EquipmentStat[] subStats; //0- max ?

    public String configID;
    public String setConfigID;
    public String rewardConfigID;
    public long snowflakeIDValue;

}

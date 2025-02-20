package com.tarantula.platform.presence.pvp;

import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.Commodity;

public class PostBattleReward extends Application {




    public int MinXPChipVariableRewardCount(){
        return header.get("MinXPChipVariableRewardCount").getAsInt();
    }
    public int MaxXPChipVariableRewardCount(){
        return header.get("MaxXPChipVariableRewardCount").getAsInt();
    }

    public Commodity hardCurrencyPerWin(){

        return null;
    }


}

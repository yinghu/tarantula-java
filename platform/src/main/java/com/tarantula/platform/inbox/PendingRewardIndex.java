package com.tarantula.platform.inbox;

import com.tarantula.platform.IndexSet;

import java.util.ArrayList;
import java.util.List;

public class PendingRewardIndex extends IndexSet {

    public PendingRewardIndex(){
        this.label = "pendingRewardIndex";
    }
    public List<PendingReward> list(){
        ArrayList<PendingReward> pendingRewards = new ArrayList<>();
        if(dataStore==null) return pendingRewards;
        keySet.forEach((k)->{
            PendingReward pendingReward = new PendingReward();
            pendingReward.distributionKey(k);
            if(this.dataStore.load(pendingReward)) pendingRewards.add(pendingReward);
        });
        return pendingRewards;
    }
}

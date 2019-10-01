package com.tarantula.demo.quest;

import com.tarantula.RecoverableFactory;
/**
 * Created by yinghu 9/30/2019
 */
public class RobotQuestQuery implements RecoverableFactory<RobotQuest> {

    private String instanceId;

    public RobotQuestQuery(String instanceId){
        this.instanceId = instanceId;
    }

    @Override
    public RobotQuest create() {
        return new RobotQuest();
    }

    @Override
    public int registryId() {
        return RobotQuestPortableRegistry.ROBOT_QUEST_OID;
    }

    @Override
    public String label() {
        return RobotQuest.LABEL;
    }

    @Override
    public String distributionKey() {
        return instanceId;
    }
}

package com.tarantula.demo.quest;

import com.tarantula.Recoverable;
import com.tarantula.platform.AbstractRecoverableListener;
/**
 * Created by yinghu 9/30/2019
 */
public class RobotQuestPortableRegistry extends AbstractRecoverableListener {

    public static final int OID = 200;

    public static final int ROBOT_QUEST_OID = 2;

    @Override
    public int registryId() {
        return OID;
    }
    @Override
    public Recoverable create(int i) {
        Recoverable pt = null;
        switch (i){
            case ROBOT_QUEST_OID:
                pt = new RobotQuest();
                break;
            default:
        }
        return pt;
    }
}

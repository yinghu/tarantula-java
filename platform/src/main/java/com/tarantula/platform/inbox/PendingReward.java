package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.presence.PresencePortableRegistry;


public class PendingReward extends Application {

    public static final String LABEL = "pending_reward";

    //name -- the name of reward like daily giveaway , achievement , tournament
    //index -- this key of the reward
    //disabled -- rewarded already
    public long configurationId;
    public PendingReward(){
        this.onEdge = true;
        this.label = LABEL;
    }
    public PendingReward(Application application){
        this();
        this.configurationId = application.distributionId();
        this.configurationName = application.configurationName();
        this.configurationTypeId = application.configurationTypeId();
        this.configurationCategory = application.configurationCategory();
        this.configurationType = application.configurationType();
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PENDING_REWARD_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(configurationId);
        buffer.writeUTF8(configurationName);
        buffer.writeUTF8(configurationType);
        buffer.writeUTF8(configurationTypeId);
        buffer.writeUTF8(configurationCategory);
        buffer.writeBoolean(disabled);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        configurationId = buffer.readLong();
        configurationName = buffer.readUTF8();
        configurationType = buffer.readUTF8();
        configurationTypeId = buffer.readUTF8();
        configurationCategory = buffer.readUTF8();
        disabled = buffer.readBoolean();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",configurationName);
        jsonObject.addProperty("TypeId",configurationTypeId);
        jsonObject.addProperty("Type",configurationType);
        jsonObject.addProperty("Category",configurationCategory);
        jsonObject.addProperty("RewardKey",this.distributionKey());
        jsonObject.addProperty("Rewarded",disabled);
        return jsonObject;
    }

    public PendingReward toApplication(){
        PendingReward pendingReward = new PendingReward(this);
        pendingReward.distributionId(this.configurationId);
        return pendingReward;
    }
}

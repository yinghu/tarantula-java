package com.tarantula.platform.presence.dailygiveaway;

import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.time.LocalDateTime;
import java.time.Year;


public class DailyLoginTrack extends RecoverableObject {

    public int lastLoginDay;
    public int tier;
    public int nextRewardTimeSeconds;
    public boolean rewardPending;

    public DailyLoginTrack(){
        this.label = "dailyLogin";
    }

    @Override
    public boolean read(DataBuffer buffer) {
        lastLoginDay = buffer.readInt();
        tier = buffer.readInt();
        timestamp = buffer.readLong();
        rewardPending = buffer.readBoolean();
        return true;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(lastLoginDay);
        buffer.writeInt(tier);
        buffer.writeLong(timestamp);
        buffer.writeBoolean(rewardPending);
        return true;
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DAILY_LOGIN_TRACK_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }


    public boolean checkDailyLogin(int pendingHours,int maxDays,int maxTier){
        if(lastLoginDay==0 && tier==0){
            lastLoginDay = 1;
            tier = 1;
            LocalDateTime cur = LocalDateTime.now();
            timestamp = TimeUtil.toUTCMilliseconds(cur);
            rewardPending = true;
            update();
            nextRewardTime(cur,pendingHours);
            return true;
        }
        LocalDateTime lastLogin = TimeUtil.fromUTCMilliseconds(this.timestamp);
        LocalDateTime current = LocalDateTime.now();
        if(lastLogin.getYear()==current.getYear()){
            int diffDays = current.getDayOfYear()-lastLogin.getDayOfYear();
            if(diffDays==0) return false;
            if(diffDays==1){
                if(TimeUtil.durationUTCInSeconds(lastLogin,current)>pendingHours*3600){
                    lastLoginDay = lastLoginDay<maxDays?(lastLoginDay+1):1;
                    if(lastLoginDay==1){
                        tier = tier<maxTier?(tier+1):1;
                    }
                    timestamp = TimeUtil.toUTCMilliseconds(current);
                    rewardPending = true;
                    update();
                    nextRewardTime(current,pendingHours);
                    return true;
                }
                else{
                    return false;
                }
            }
            lastLoginDay = 1;
            tier = tier<maxTier?(tier+1):1;
            timestamp = TimeUtil.toUTCMilliseconds(current);
            rewardPending = true;
            update();
            nextRewardTime(current,pendingHours);
            return true;
        }
        else{
            int lastDayOfYear = Year.isLeap(lastLogin.getYear())?366:365;
            if(lastLoginDay==lastDayOfYear&&current.getDayOfYear()==1){
                lastLoginDay = lastLoginDay<maxDays?(lastLoginDay+1):1;
                if(lastLoginDay==1){
                    tier = tier<maxTier?(tier+1):1;
                }
            }
            else{
                lastLoginDay = 1;
                tier = tier<maxTier?(tier+1):1;
            }
            rewardPending = true;
            timestamp = TimeUtil.toUTCMilliseconds(current);
            update();
            nextRewardTime(current,pendingHours);
            return true;
        }
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("LastLoginDay",lastLoginDay);
        jsonObject.addProperty("RewardTier",tier);
        jsonObject.addProperty("RewardKey",rewardKey());
        jsonObject.addProperty("NextLoginInSeconds",nextRewardTimeSeconds);
        jsonObject.addProperty("RewardPending",rewardPending);
        return jsonObject;
    }
    public String rewardKey(){
        return "t_"+tier+"_d_"+lastLoginDay;
    }
    public void reset(){
        lastLoginDay = 0;
        tier = 0;
        update();
    }
    private void nextRewardTime(LocalDateTime current,int pendingHours){
        int remainingSeconds = 24*60*60-current.getSecond();
        nextRewardTimeSeconds = remainingSeconds>pendingHours*60*60?remainingSeconds:pendingHours*60*60;
    }

    public static DailyLoginTrack lookup(long saveId, DataStore dataStore){
        DailyLoginTrack dailyLoginTrack = new DailyLoginTrack();
        dailyLoginTrack.distributionId(saveId);
        dataStore.createIfAbsent(dailyLoginTrack,true);
        dailyLoginTrack.dataStore(dataStore);
        return dailyLoginTrack;
    }

}

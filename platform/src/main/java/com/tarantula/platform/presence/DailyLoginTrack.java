package com.tarantula.platform.presence;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AssociateKey;


import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DailyLoginTrack extends RecoverableObject {

    public int lastLoginDay;
    public int rewardTier;

    public DailyLoginTrack(){
        this.label = "dailyLogin";
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",lastLoginDay);
        this.properties.put("2",rewardTier);
        this.properties.put("3",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.lastLoginDay = ((Number) properties.get("1")).intValue();
        this.rewardTier = ((Number) properties.get("2")).intValue();
        this.timestamp = ((Number) properties.get("3")).longValue();
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.DAILY_LOGIN_TRACK_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }

    public boolean checkDailyLogin(int pendingHours,int maxDays,int maxTier){
        if(lastLoginDay==0&&rewardTier==0){
            lastLoginDay = 1;
            rewardTier = 1;
            timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
            dataStore.update(this);
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
                        rewardTier = rewardTier<maxTier?(rewardTier+1):1;
                    }
                    timestamp = TimeUtil.toUTCMilliseconds(current);
                    this.dataStore.update(this);
                    return true;
                }
                else{
                    return false;
                }
            }
            lastLoginDay = 1;
            rewardTier = rewardTier<maxTier?(rewardTier+1):1;
            timestamp = TimeUtil.toUTCMilliseconds(current);
            this.dataStore.update(this);
            return true;
        }
        else{
            int lastDayOfYear = Year.isLeap(lastLogin.getYear())?366:365;
            if(lastLoginDay==lastDayOfYear&&current.getDayOfYear()==1){
                lastLoginDay = lastLoginDay<maxDays?(lastLoginDay+1):1;
                if(lastLoginDay==1){
                    rewardTier = rewardTier<maxTier?(rewardTier+1):1;
                }
            }
            else{
                lastLoginDay = 1;
                rewardTier = rewardTier<maxTier?(rewardTier+1):1;
            }
            timestamp = TimeUtil.toUTCMilliseconds(current);
            dataStore.update(this);
            return true;
        }
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("lastLoginDay",lastLoginDay);
        jsonObject.addProperty("rewardTier",rewardTier);
        jsonObject.addProperty("lastLogin",TimeUtil.fromUTCMilliseconds(this.timestamp).format(DateTimeFormatter.ISO_DATE_TIME));
        return jsonObject;
    }
}

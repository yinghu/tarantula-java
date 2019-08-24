package com.tarantula.platform.leveling;


import com.tarantula.Level;
import com.tarantula.Recoverable;
import com.tarantula.XP;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.nio.ByteBuffer;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Developer: YINGHU LU
 * Date Updated: 8/24/19
 * Time: 4:41 PM
 */
public class XPLevel extends RecoverableObject implements Level {

    private int level;

    public LevelView levelView;
    private double levelXP;
    private HashMap<String,XP> xpMapping = new HashMap<>();

    private LocalDateTime dailyTimestamp;
    private LocalDateTime weeklyTimestamp;
    private LocalDateTime monthlyTimestamp;
    private LocalDateTime yearlyTimestamp;

    public XPLevel(){
        this.vertex = "XPLevel";
        this.binary = true;
    }
    @Override
    public int getFactoryId() {
        return LevelingPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return LevelingPortableRegistry.LEVEL_CID;
    }



    @Override
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(44);
        buffer.putInt(level);
        buffer.putDouble(levelXP);
        buffer.putLong(dailyTimestamp.toInstant(ZoneOffset.UTC).toEpochMilli());
        buffer.putLong(weeklyTimestamp.toInstant(ZoneOffset.UTC).toEpochMilli());
        buffer.putLong(monthlyTimestamp.toInstant(ZoneOffset.UTC).toEpochMilli());
        buffer.putLong(yearlyTimestamp.toInstant(ZoneOffset.UTC).toEpochMilli());
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        level = buffer.getInt();
        levelXP = buffer.getDouble();
        dailyTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(buffer.getLong()),ZoneOffset.UTC);
        weeklyTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(buffer.getLong()),ZoneOffset.UTC);
        monthlyTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(buffer.getLong()),ZoneOffset.UTC);
        yearlyTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(buffer.getLong()),ZoneOffset.UTC);
    }
    @Override
    public String toString(){
        return "Level ("+this.oid+") ["+this.level+","+levelXP+"]";
    }


    public int level() {
        return this.level;
    }


    public void level(int level) {
        this.level = level;
    }


    public double levelXP(double levelXP){
        this.levelXP = this.levelXP+levelXP;
        return this.levelXP;
    }
    public boolean onDailyGainReset(){
        LocalDateTime _cur = LocalDateTime.now(ZoneOffset.UTC);
        if(dailyTimestamp!=null&&dailyTimestamp.isAfter(_cur)){
            return false;
        }
        else{
            LocalDateTime d = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
            dailyTimestamp= d.plusDays(1);
            return true;
        }
    }
    public boolean onWeeklyGainReset(){
        LocalDateTime _cur = LocalDateTime.now(ZoneOffset.UTC);
        if(weeklyTimestamp!=null&&weeklyTimestamp.isAfter(_cur)){
            return false;
        }
        else{
            LocalDateTime d = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
            weeklyTimestamp= d.plusDays(7-d.getDayOfWeek().getValue());
            return true;
        }
    }
    public boolean onMonthlyGainReset(){
        LocalDateTime _cur = LocalDateTime.now(ZoneOffset.UTC);
        if(monthlyTimestamp!=null&&monthlyTimestamp.isAfter(_cur)){
            return false;
        }
        else{
            LocalDateTime d = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
            monthlyTimestamp= d.plusMonths(1).withDayOfMonth(1);
            return true;
        }
    }
    public boolean onYearlyGainReset(){
        LocalDateTime _cur = LocalDateTime.now(ZoneOffset.UTC);
        if(yearlyTimestamp!=null&&yearlyTimestamp.isAfter(_cur)){
            return false;
        }
        else{
            LocalDateTime d = LocalDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.MIDNIGHT);
            yearlyTimestamp= d.plusYears(1).withDayOfYear(1);
            return true;
        }
    }
    public void xp(XP xp){
        String qk = new StringBuffer(xp.header()).append("_").append(xp.category()).toString();
        xpMapping.put(qk,xp);
    }
    public List<XP> list(String header,String category){
        String qk = new StringBuffer(header).append("_").append(category).toString();
        ArrayList<XP> _gxp = new ArrayList<>();
        xpMapping.forEach((String k,XP xp)->{
            if(k.equals(qk)){
                _gxp.add(xp);
            }
        });
        return _gxp;
    }
    public List<XP> list(){
        ArrayList<XP> _gxp = new ArrayList<>();
        xpMapping.forEach((String k,XP xp)->{
            _gxp.add(xp);
        });
        return _gxp;
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
}

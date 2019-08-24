package com.tarantula.platform.leveling;

import com.tarantula.LeaderBoard;
import com.tarantula.XP;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;

import java.nio.ByteBuffer;

/**
 * Updated by yinghu lu on 8/24/19
 */
public class XPGain extends RecoverableObject implements XP {

    private double dailyGain;
    private double weeklyGain;
    private double monthlyGain;
    private double yearlyGain;
    private double totalGain;

    private String header;
    private String category;

    public XPGain(){
        this.vertex = "XPGain";
        this.label = "LXG";
        this.binary = true;
        this.onEdge = true;
    }
    public XPGain(String owner,String bucket,String oid,String header,String category){
        this();
        this.owner = owner;
        this.bucket = bucket;
        this.oid = oid;
        this.header = header;
        this.category = category;
    }
    @Override
    public int getFactoryId() {
        return LevelingPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return LevelingPortableRegistry.XP_CID;
    }

    @Override
    public byte[] toByteArray(){
        byte[] _hb = header.getBytes();
        byte[] _cb = category.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(48+_hb.length+_cb.length);
        buffer.putDouble(dailyGain);
        buffer.putDouble(weeklyGain);
        buffer.putDouble(monthlyGain);
        buffer.putDouble(yearlyGain);
        buffer.putDouble(totalGain);
        buffer.putInt(_hb.length);
        buffer.put(_hb);
        buffer.putInt(_cb.length);
        buffer.put(_cb);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        dailyGain = buffer.getDouble();
        weeklyGain = buffer.getDouble();
        monthlyGain = buffer.getDouble();
        yearlyGain = buffer.getDouble();
        totalGain = buffer.getDouble();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        header = sb.toString();
        len = buffer.getInt();
        sb.setLength(0);
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        this.category = sb.toString();
    }

    public void reset(String xName){
        if(xName.equals(LeaderBoard.DAILY)){
            dailyGain = 0;
        }
        else if(xName.equals(LeaderBoard.WEEKLY)){
            weeklyGain = 0;
        }
        else if(xName.equals(LeaderBoard.MONTHLY)){
            monthlyGain = 0;
        }
        else if(xName.equals(LeaderBoard.YEARLY)){
            yearlyGain = 0;
        }
    }

    public String header(){
        return this.header;
    }
    public String category(){
        return this.category;
    }
    public void  header(String header){
        this.header = header;
    }
    public void category(String category){
        this.category = category;
    }

    public LeaderBoard.Entry dailyGain(double delta){
        this.dailyGain = dailyGain+delta;
        return new LeaderBoardEntry(header,category,LeaderBoard.DAILY,dailyGain,System.currentTimeMillis());
    }
    public LeaderBoard.Entry weeklyGain(double delta){
        this.weeklyGain = weeklyGain+delta;
        return new LeaderBoardEntry(header,category,LeaderBoard.WEEKLY,weeklyGain,System.currentTimeMillis());
    }
    public LeaderBoard.Entry monthlyGain(double delta){
        this.monthlyGain = monthlyGain+delta;
        return new LeaderBoardEntry(header,category,LeaderBoard.MONTHLY,monthlyGain,System.currentTimeMillis());
    }
    public LeaderBoard.Entry yearlyGain(double delta){
        this.yearlyGain = yearlyGain+delta;
        return new LeaderBoardEntry(header,category,LeaderBoard.YEARLY,yearlyGain,System.currentTimeMillis());
    }
    public LeaderBoard.Entry totalGain(double delta){
        this.totalGain = totalGain+delta;
        return new LeaderBoardEntry(header,category,LeaderBoard.TOTAL,totalGain,System.currentTimeMillis());
    }
    @Override
    public Key key(){
        return new ResourceKey(bucket,oid,new String[]{this.header,this.category});
    }
}

package com.tarantula.platform.leveling;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.OnLeaderBoard;
import com.tarantula.XP;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.ResourceKey;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Updated by yinghu lu on 4/23/2018.
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
    public void writePortable(PortableWriter out) throws IOException {
        super.writePortable(out);
        out.writeUTF("2",(String)this.properties.get("name"));
        out.writeDouble("3",(Double)this.properties.get("Daily"));
        out.writeDouble("4",(Double)this.properties.get("Weekly"));
        out.writeDouble("5",(Double)this.properties.get("Monthly"));
        out.writeDouble("6",(Double)this.properties.get("Yearly"));
        out.writeDouble("7",(Double)this.properties.get("Total"));

    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        super.readPortable(in);
        this.properties.put("name",in.readUTF("2"));
        this.properties.put("Daily",in.readDouble("3"));
        this.properties.put("Weekly",in.readDouble("4"));
        this.properties.put("Monthly",in.readDouble("5"));
        this.properties.put("Yearly",in.readDouble("6"));
        this.properties.put("Total",in.readDouble("7"));

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
        if(xName.equals(OnLeaderBoard.DAILY)){
            dailyGain = 0;
        }
        else if(xName.equals(OnLeaderBoard.WEEKLY)){
            weeklyGain = 0;
        }
        else if(xName.equals(OnLeaderBoard.MONTHLY)){
            monthlyGain = 0;
        }
        else if(xName.equals(OnLeaderBoard.YEARLY)){
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

    public double dailyGain(double delta){
        this.dailyGain = dailyGain+delta;
        return dailyGain;
    }
    public double weeklyGain(double delta){
        this.weeklyGain = weeklyGain+delta;
        return weeklyGain;
    }
    public double monthlyGain(double delta){
        this.monthlyGain = monthlyGain+delta;
        return monthlyGain;
    }
    public double yearlyGain(double delta){
        this.yearlyGain = yearlyGain+delta;
        return yearlyGain;
    }
    public double totalGain(double delta){
        this.totalGain = totalGain+delta;
        return totalGain;
    }
    @Override
    public Key key(){
        return new ResourceKey(bucket,oid,new String[]{this.header,this.category});
    }
}

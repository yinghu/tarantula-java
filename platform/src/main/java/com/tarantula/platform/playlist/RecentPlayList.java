package com.tarantula.platform.playlist;

import com.tarantula.OnPlay;
import com.tarantula.PlayList;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.util.DrainBuffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Updated by yinghu on 4/24/2018
 */
public class RecentPlayList extends RecoverableObject implements PlayList{

    private String name;
    private int size;
    private DrainBuffer<OnPlay> list;

    public RecentPlayList(){
        this.binary = true;
        this.vertex = "RecentlyPlayList";
    }

    public String name() {
        return name;
    }
    public int size(){
        return this.size;
    }
    public void size(int size){
        this.size = size;
    }
    public void name(String name) {
        this.name = name;
    }
    public boolean open(){
        return true;
    }
    public void open(boolean open){

    }

    public int getFactoryId() {
        return BuddyListPortableRegistry.OID;
    }


    public int getClassId() {
        return BuddyListPortableRegistry.RECENT_PLAY_LIST_CID;
    }



    public List<OnPlay> onPlay(){
        return this.list.drain(new ArrayList<>());
    }
    public boolean onPlay(OnPlay onPlay){
        this.list.push(onPlay);
        return true;
    }
    public void start(){
        list = new DrainBuffer(size,new OnPlay[size]);
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("size",this.size);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.size = ((Long)properties.get("size")).intValue();
    }
    public byte[] toByteArray(){
        byte[] _nb = name.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(8+_nb.length);
        buffer.putInt(this.size);
        buffer.putInt(_nb.length);
        buffer.put(_nb);
        return buffer.array();
    }
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.size = buffer.getInt();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        name = sb.toString();
    }
}

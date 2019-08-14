package com.tarantula.platform.playlist;

import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.tarantula.*;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.RecoverableObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated by yinghu on 4/24/2018.
 */
public class BuddyList extends RecoverableObject implements PlayList{

    private String name="My List";
    private int size;
    private boolean open;

    private ConcurrentHashMap<String,OnPlay> links = new ConcurrentHashMap<>();

    public BuddyList() {
        vertex = "BuddyList";
        this.binary = true;
    }
    public BuddyList(String systemId) {
        this();
        this.owner = systemId;
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
         return this.open;
    }
    public void open(boolean open){
       this.open = open;
    }
    public int getFactoryId() {
        return BuddyListPortableRegistry.OID;
    }
    @Override
    public void writePortable(PortableWriter out) throws IOException {
        super.writePortable(out);
        out.writeInt("2",this.size);
    }
    @Override
    public void readPortable(PortableReader in) throws IOException {
        super.readPortable(in);
        this.size = in.readInt("2");
    }

    public byte[] toByteArray(){
        byte[] _nb = name.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(9+_nb.length);
        buffer.putInt(this.size);
        buffer.put(open?(byte)1:0);
        buffer.putInt(_nb.length);
        buffer.put(_nb);
        return buffer.array();
    }
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.size = buffer.getInt();
        this.open = buffer.get()==1?true:false;
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        name = sb.toString();
    }
    public int getClassId() {
        return BuddyListPortableRegistry.MY_PLAY_LIST_CID;
    }


    public List<OnPlay> onPlay(){
        ArrayList<OnPlay> alist = new ArrayList<>();
        links.forEach((k,v)->alist.add(v));
        return alist;
    }
    public boolean onPlay(OnPlay onPlay){
        return this.links.putIfAbsent(onPlay.systemId(),onPlay)==null;
    }


    @Override
    public Key key(){
        return new AssociateKey(this.bucket,this.oid,this.vertex);
    }
    @Override
    public String toString(){
        return "Buddy Play List ["+name+"/"+size+"/"+open+"]";
    }
}

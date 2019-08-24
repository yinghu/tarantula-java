package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;
import com.tarantula.Recoverable;
import com.tarantula.platform.NaturalKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.ResourceKey;
import com.tarantula.platform.util.SystemUtil;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Updated 8/24/19
 */
public class Top10LeaderBoard extends RecoverableObject implements LeaderBoard {

    private String name;
    private String header;
    private String category;
    private String classifier;
    private int size;
    private Entry[] entryList;

    private ConcurrentHashMap<String,Entry> entryIndex = new ConcurrentHashMap<>();

    private EntryComparator comparator = new EntryComparator();

    private Reset reset;
    private DataStore dataStore;

    public Top10LeaderBoard(){
        this.vertex = "Top10LeaderBoard";
        this.label = "TLP";
        this.onEdge = true;
        //this.binary = true;
    }
    public Top10LeaderBoard(String name, String header, String category, String classifier, int size, DataStore dataStore){
        this();
        this.name = name;
        this.header = header;
        this.category = category;
        this.classifier = classifier;
        this.size = size;
        this.entryList = new Entry[size];
        this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now());
        this.dataStore = dataStore;
    }
    public void preload(DataStore dataStore){
        this.entryList = new Entry[size];
        this.dataStore = dataStore;
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void size(int size) {
        this.size = size;
    }

    @Override
    public String leaderBoardHeader() {
        return header;
    }

    @Override
    public void leaderBoardHeader(String header) {
        this.header = header;
    }

    @Override
    public String classifier() {
        return classifier;
    }

    @Override
    public void classifier(String classifier) {
        this.classifier = classifier;
    }

    @Override
    public String category() {
        return category;
    }

    @Override
    public void category(String category) {
        this.category = category;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.name);
        this.properties.put("2",this.header);
        this.properties.put("3",category);
        this.properties.put("4",this.classifier);
        this.properties.put("5",this.size);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = (String) properties.get("1");
        this.header = (String) properties.get("2");
        this.category = (String) properties.get("3");
        this.classifier = (String) properties.get("4");
        this.size = ((Number)properties.get("5")).intValue();
    }
    @Override
    public byte[] toByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(size);
        buffer.putLong(timestamp);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.size = buffer.getInt();
        this.timestamp = buffer.getLong();
    }

    @Override
    public void onBoard(String systemId, LeaderBoard.Entry entry) {
        Entry last = entryList[9];
        if(entry.value()>=last.value()){
            Entry ix = entryIndex.get(systemId);
            if(ix==null){//kick off last
                entryIndex.remove(last.systemId());
                entryIndex.put(systemId,last);
                last.update(systemId,entry.value());
                if(dataStore!=null){
                    dataStore.update(last);
                }
            }
            else{//update existing entry
                ix.value(entry.value());
                if(dataStore!=null){
                    dataStore.update(ix);
                }
            }
            Arrays.sort(entryList,comparator);
        }
    }

    @Override
    public Entry[] list() {
        return entryList;
    }
    public void entry(int index,Entry entry){
        if(!entry.systemId().equals("--")){
            entryIndex.put(entry.systemId(),entry);
        }
        entryList[index]=entry;
    }
    public void registerReset(Reset reset){
        this.reset = reset;
    }
    public void reset(){
        if(reset.reset(this)){
            if(this.dataStore!=null){
                this.dataStore.update(this);
            }
            for(Entry e : entryList){
                e.update("--",0);
                if(this.dataStore!=null){
                    this.dataStore.update(e);
                }
            }
            entryIndex.clear();
        }
    }
    @Override
    public int getFactoryId() {
        return LeaderBoardPortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return LeaderBoardPortableRegistry.TOP10_LEADER_BOARD_CID;
    }
    public Key key(){
        return new NaturalKey(this.name+ Recoverable.PATH_SEPARATOR+header+Recoverable.PATH_SEPARATOR+category+Recoverable.PATH_SEPARATOR+classifier);
    }
    public String toString(){
        return this.name+"/"+header+"/"+category+"/"+classifier+"/"+size;
    }
}

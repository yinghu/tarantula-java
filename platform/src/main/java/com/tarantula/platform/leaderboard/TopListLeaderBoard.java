package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;
import com.tarantula.Recoverable;
import com.tarantula.platform.NaturalKey;
import com.tarantula.platform.RecoverableObject;
import com.tarantula.platform.util.SystemUtil;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Updated 8/24/19
 */
public class TopListLeaderBoard extends RecoverableObject implements LeaderBoard {

    private String name;
    private String header;
    private String category;
    private String classifier;
    private int size;
    private Entry[] entryList;

    private HashMap<String,Entry> entryIndex = new HashMap<>();

    private EntryComparator comparator = new EntryComparator();

    private Reset reset;
    private DataStore dataStore;
    private boolean updating;

    public TopListLeaderBoard(){
        this.vertex = "TopListLeaderBoard";
        this.label = "ldb";
    }
    public TopListLeaderBoard(String name, String header, String category, String classifier, int size, DataStore dataStore, boolean updating){
        this();
        this.name = name;
        this.header = header;
        this.category = category;
        this.classifier = classifier;
        this.size = size;
        this.entryList = new Entry[size];
        this.timestamp = SystemUtil.toUTCMilliseconds(LocalDateTime.now());
        this.dataStore = dataStore;
        this.updating = updating;
    }
    @Override
    public String name() {
        return name;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public String header() {
        return header;
    }
    @Override
    public String classifier() {
        return classifier;
    }
    @Override
    public String category() {
        return category;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",this.size);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.size = ((Number)properties.get("1")).intValue();
    }


    @Override
    public synchronized boolean onBoard(String systemId, LeaderBoard.Entry entry) {
        Entry last = entryList[size-1];
        if(entry.value()>=last.value()){//update on board
            Entry ix = entryIndex.get(systemId);
            if(ix==null){//kick off last
                entryIndex.remove(last.systemId());
                entryIndex.put(systemId,last);
                last.update(systemId,entry.value(),entry.timestamp());
                if(updating){
                    dataStore.update(last);
                }
            }
            else{//update existing entry
                ix.update(systemId,entry.value(),entry.timestamp());
                if(updating){
                    dataStore.update(ix);
                }
            }
            Arrays.sort(entryList,comparator);
            return true;
        }
        else{
            return false;
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
            for(Entry e : entryList){
                e.update("--",0,0);
                if(updating){
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
    public void distributionKey(String distributionKey){
        //skip the natural key
    }
    public Key key(){
        return new NaturalKey(this.name+ Recoverable.PATH_SEPARATOR+header+Recoverable.PATH_SEPARATOR+category+Recoverable.PATH_SEPARATOR+classifier);
    }
    public String toString(){
        return this.key().asString()+"["+size+"]";
    }
}

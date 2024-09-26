package com.tarantula.platform.presence.leaderboard;

import com.icodesoftware.LeaderBoard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

public class BoardSync extends RecoverableObject{

    public String classifier;
    public String category;
    private int size;
    private LeaderBoardEntry[] board;
    private HashMap<Long, LeaderBoardEntry> eIndex;
    private EntryComparator entryComparator;
    private final AtomicBoolean load = new AtomicBoolean(false);
    public BoardSync(String classifier, String category, int size, EntryComparator entryComparator){
        this.classifier = classifier;
        this.category = category;
        this.size = size;
        this.entryComparator = entryComparator;
    }
    public synchronized void load(LeaderBoard.Listener listener){
        if(load.getAndSet(true)) return;
        board = new LeaderBoardEntry[size];
        eIndex = new HashMap<>();
        int[] ix = {0};
        dataStore.list(new LeaderBoardEntryQuery(distributionId,classifier,category),e->{
            board[ix[0]++]=e;
            e.dataStore(dataStore);
            if(e.systemId()>0) {
                eIndex.put(e.systemId(),e);
                listener.onUpdated(e);
            }
            return true;
        });
        if(ix[0]>0){
            Arrays.sort(board,entryComparator);
            return;
        }
        for(int i=0;i<size;i++){
            LeaderBoardEntry entry = new LeaderBoardEntry(classifier,category);
            entry.ownerKey(SnowflakeKey.from(distributionId));
            this.dataStore.create(entry);
            entry.dataStore(dataStore);
            board[i]=entry;
        }
    }
    public synchronized void onBoard(LeaderBoard.Entry entry,LeaderBoard.Listener listener){
        LeaderBoardEntry e = eIndex.get(entry.systemId());
        if(e==null && (e=board[size-1]).value() < entry.value()){
            eIndex.remove(e.systemId());
            e.update(entry).update();
            eIndex.put(e.systemId(),e);
            Arrays.sort(board,entryComparator);
            listener.onUpdated(e);
            return;
        }
        if(e!=null && e.value() < entry.value()){
            e.update(entry).update();
            Arrays.sort(board,entryComparator);
            listener.onUpdated(e);
        }
    }
    public synchronized  void reset(){
        eIndex.clear();
        for(LeaderBoardEntry e : board){
            e.reset().update();
        }
    }
    public boolean onBoard(long systemId){
        return eIndex.containsKey(systemId);
    }

    public int size(){
        return size;
    }
}

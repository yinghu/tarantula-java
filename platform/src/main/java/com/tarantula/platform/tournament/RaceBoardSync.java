package com.tarantula.platform.tournament;

import com.icodesoftware.DataStore;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.SnowflakeKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RaceBoardSync extends RecoverableObject {

    private final int size;
    private TournamentEntry[] board;
    private HashMap<Long,TournamentEntry> entryIndex;
    private TournamentEntryComparator entryComparator;


    public RaceBoardSync(int size,TournamentEntryComparator entryComparator){
        this.size = size;
        this.entryComparator = entryComparator;
    }

    public synchronized void load(){
        board = new TournamentEntry[size];
        entryIndex = new HashMap<>();
        //LOAD
        TournamentEntryQuery query = new TournamentEntryQuery(this.distributionId);
        int[] index = {0};
        dataStore.list(query,entry->{
            board[index[0]++]=entry;
            if(entry.systemId()>0) entryIndex.put(entry.systemId(),entry);
            return true;
        });
        //CREATE IF NO ITEMS LOADED
        if(index[0]==0){
            for(int i=0;i<size;i++){
                TournamentEntry entry = new TournamentEntry();
                entry.ownerKey(SnowflakeKey.from(distributionId));
                dataStore.create(entry);
                board[i]=entry;
            }
        }
    }

    public synchronized void onBoard(TournamentEntry pendingEntry){
        TournamentEntry entry = entryIndex.get(pendingEntry.systemId());
        if(entry == null && (entry = board[size-1]).score() < pendingEntry.score()){
            entryIndex.remove(entry.systemId());
            entry.update(pendingEntry);
            entryIndex.put(pendingEntry.systemId(),entry);
            Arrays.sort(board,entryComparator);
            return;
        }
        if(entry != null && entry.score() < pendingEntry.score()){
            entry.update(pendingEntry);
            Arrays.sort(board,entryComparator);
        }
    }
    public int size(){
        return size;
    }

    public synchronized Tournament.Entry onBoard(long systemId){
        if(!entryIndex.containsKey(systemId)) return null;
        return entryIndex.get(systemId).duplicate(0);
    }

    public synchronized List<Tournament.Entry> snapshot(){
        ArrayList<Tournament.Entry> list = new ArrayList<>();
        for(int i=0;i<size;i++){
            if(board[i].systemId()==0) continue;
            list.add(board[i].duplicate(i+1));
        }
        return list;
    }
}

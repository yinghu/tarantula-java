package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.RecoverableObject;
import java.util.Arrays;
import java.util.HashMap;


public class BoardSync extends RecoverableObject{

    private String classifier;
    private String category;
    private int size;
    private LeaderBoardEntry[] board;
    private HashMap<String, LeaderBoardEntry> eIndex;
    private EntryComparator entryComparator;

    public BoardSync(String classifier, String category, int size, EntryComparator entryComparator){
        this.classifier = classifier;
        this.category = category;
        this.size = size;
        this.entryComparator = entryComparator;
    }
    public synchronized void load(LeaderBoard.Listener listener){
        board = new LeaderBoardEntry[size];
        eIndex = new HashMap<>();
        for(int i=0;i<size;i++){
            LeaderBoardEntry entry = new LeaderBoardEntry(classifier,category,i);
            this.dataStore.createIfAbsent(entry,true);
            entry.dataStore(dataStore);
            board[i]=entry;
            if(entry.value()>0){
                eIndex.put(entry.owner(),entry);
                listener.onUpdated(entry);
            }
        }
        Arrays.sort(board,entryComparator);
    }
    synchronized void onBoard(LeaderBoard.Entry entry,LeaderBoard.Listener listener){
        LeaderBoardEntry e = eIndex.get(entry.owner());
        if(e==null&&(e=board[size-1]).value()<entry.value()){
            eIndex.remove(e.owner());
            e.update(entry).update();
            eIndex.put(e.owner(),e);
            Arrays.sort(board,entryComparator);
            listener.onUpdated(e);
        }
        else if(e!=null&&e.value()<entry.value()){
            e.update(entry).update();
            Arrays.sort(board,entryComparator);
            listener.onUpdated(e);
        }
    }
    synchronized  void reset(){
        //reset board entry with --,0
        eIndex.clear();
        for(LeaderBoardEntry e : board){
            e.reset().update();
        }
    }
}

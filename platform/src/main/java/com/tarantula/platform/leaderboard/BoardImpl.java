package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.RecoverableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class BoardImpl extends RecoverableObject implements LeaderBoard.Board {

    private String classifier;
    private String category;
    private int size;
    private EntryImpl[] board;
    private HashMap<String,EntryImpl> eIndex;
    private EntryComparator entryComparator;

    public BoardImpl(String classifier,String category,int size,EntryComparator entryComparator){
        this.classifier = classifier;
        this.category = category;
        this.size = size;
        this.entryComparator = entryComparator;
    }
    public void load(LeaderBoard.Listener listener){
        board = new EntryImpl[size];
        eIndex = new HashMap<>();
        for(int i=0;i<size;i++){
            EntryImpl entry = new EntryImpl(classifier,category,i);
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
    public synchronized void onBoard(LeaderBoard.Entry entry,LeaderBoard.Listener listener){
        EntryImpl e = eIndex.get(entry.owner());
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
    @Override
    public synchronized LeaderBoard.Entry[] list() {
        for(EntryImpl e: board){
            System.out.println(e.toString());
        }
        return board;
    }
}

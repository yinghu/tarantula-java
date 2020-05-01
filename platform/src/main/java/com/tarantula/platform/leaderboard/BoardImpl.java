package com.tarantula.platform.leaderboard;

import com.tarantula.LeaderBoard;
import com.tarantula.platform.RecoverableObject;


public class BoardImpl extends RecoverableObject implements LeaderBoard.Board {

    private String classifier;
    private String category;
    private int size;
    private EntryImpl[] board;
    public BoardImpl(String classifier,String category,int size){
        this.classifier = classifier;
        this.category = category;
        this.size = size;
    }
    public void load(){
        board = new EntryImpl[size];
        for(int i=0;i<size;i++){
            EntryImpl entry = new EntryImpl(classifier,category,i);
            this.dataStore.createIfAbsent(entry,true);
            entry.dataStore(dataStore);
            board[i]=entry;
        }
    }
    public void onBoard(LeaderBoard.Entry entry,LeaderBoard.Listener listener){
        board[0].update(entry).update();
        listener.onUpdated(board[0]);
    }
    @Override
    public LeaderBoard.Entry[] list() {
        //
        return board;
    }
}

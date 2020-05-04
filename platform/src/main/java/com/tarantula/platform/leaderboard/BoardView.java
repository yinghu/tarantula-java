package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by yinghu on 5/2/2020
 */
public class BoardView implements LeaderBoard.Board {


    private final BoardSync sync;
    private final LeaderBoard.Listener listener;
    private final Comparator<LeaderBoard.Entry> comparator;

    private HashMap<String, LeaderBoard.Entry> vIndex;
    private List<LeaderBoard.Entry> vList;
    public BoardView(BoardSync board, LeaderBoard.Listener listener, Comparator<LeaderBoard.Entry> comparator){
        this.sync = board;
        this.listener = listener;
        this.comparator = comparator;
        this.vIndex = new HashMap<>();
        this.vList = new CopyOnWriteArrayList<>();
    }
    @Override
    public void onBoard(String systemId, double value) {
        sync.onBoard(new LeaderBoardEntry(systemId,value,System.currentTimeMillis()),listener);
    }
    //check global list
    synchronized boolean onView(LeaderBoard.Entry entry){
        LeaderBoard.Entry e;
        if((e=vIndex.get(entry.owner()))==null){
            vIndex.put(entry.owner(),entry);
            vList.add(entry);
        }
        else{
            e.update(entry);
        }
        Collections.sort(vList,comparator);
        return true;
    }
    public void rank(LeaderBoard.Stream ranking){
        int rank = 1;
        for(LeaderBoard.Entry e: vList){
            ranking.onRank(rank++,e);
        }
    }
    void reset(){
        vIndex.clear();
        vList.clear();
        this.sync.reset();
    }
    @Override
    public void dataStore(DataStore dataStore) {

    }

    @Override
    public void update() {

    }
}

package com.tarantula.platform.presence.leaderboard;

import com.icodesoftware.LeaderBoard;
import com.icodesoftware.util.RecoverableObject;

import java.util.*;

public class BoardView extends RecoverableObject implements LeaderBoard.Board,LeaderBoard.Listener {


    private final BoardSync sync;
    private final LeaderBoard.Listener listener;
    private final Comparator<LeaderBoard.Entry> comparator;

    private HashMap<Long, LeaderBoard.Entry> vIndex;
    private ArrayList<LeaderBoard.Entry> vList;

    public BoardView(BoardSync board, LeaderBoard.Listener listener, Comparator<LeaderBoard.Entry> comparator){
        this.sync = board;
        this.listener = listener;
        this.comparator = comparator;
        this.vIndex = new HashMap<>();
        this.vList =  new ArrayList<>();
    }
    @Override
    public synchronized void onBoard(long systemId, double value) {
        LeaderBoard.Entry existing = vIndex.get(systemId);
        if(existing!=null){
            existing.update(new LeaderBoardEntry(systemId,value,System.currentTimeMillis()));
            this.listener.onUpdated(existing);
            Collections.sort(vList,comparator);
            return;
        }
        if(vList.size()<sync.size()){
            LeaderBoardEntry entry = new LeaderBoardEntry(systemId,value,System.currentTimeMillis());
            vList.add(entry);
            vIndex.put(systemId,entry);
            this.listener.onUpdated(entry);
            Collections.sort(vList,comparator);
            return;
        }
        LeaderBoard.Entry last = vList.get(sync.size()-1);
        if(last.value() > value) return;
        last.update(new LeaderBoardEntry(systemId,value,System.currentTimeMillis()));
        this.listener.onUpdated(last);
        Collections.sort(vList,comparator);
    }

    public void load(){
        this.sync.load(this);
        Collections.sort(vList,comparator);
    }

    public synchronized void onSync(LeaderBoard.Entry entry){
        sync.onBoard(entry,listener);
    }
    public void rank(LeaderBoard.Listener ranking){
        int rank = 1;
        for(LeaderBoard.Entry e: vList){
            e.rank(rank++);
            ranking.onUpdated(e);
        }
    }
    public void reset(){
        vIndex.clear();
        vList.clear();
        sync.load(this);
    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        vList.add(entry);
        vIndex.put(entry.systemId(),entry);
    }
}

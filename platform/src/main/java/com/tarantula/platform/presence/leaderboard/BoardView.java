package com.tarantula.platform.presence.leaderboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    public synchronized void onBoard(LeaderBoard.Entry update) {
        LeaderBoard.Entry existing = vIndex.get(update.systemId());
        if(existing!=null){
            existing.update(update);
            this.listener.onUpdated(existing);
            Collections.sort(vList,comparator);
            return;
        }
        if(vList.size() < sync.size()){
            vList.add(update);
            vIndex.put(update.systemId(),update);
            this.listener.onUpdated(update);
            Collections.sort(vList,comparator);
            return;
        }
        LeaderBoard.Entry last = vList.get(sync.size()-1);
        if(last.value() > update.value()) return;
        last.update(update);
        this.listener.onUpdated(last);
        Collections.sort(vList,comparator);
    }

    public void load(){
        this.sync.load(this);
        Collections.sort(vList,comparator);
    }

    public void onSync(LeaderBoard.Entry entry, LeaderBoard.Listener listener){
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

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        int rank = 1;
        JsonArray list = new JsonArray();
        for(LeaderBoard.Entry e: vList){
            e.rank(rank++);
            list.add(e.toJson());
        }
        jsonObject.add("_board",list);
        return jsonObject;
    }
}

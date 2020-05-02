package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;

/**
 * Updated 8/24/19
 */
public class LeaderBoardSync implements LeaderBoard{


    private String category;
    private int size;

    private BoardSync[] boards = new BoardSync[3];

    private EntryComparator comparator = new EntryComparator();
    private Listener listener;
    private DataStore dataStore;

    private BoardView[] views = new BoardView[3];


    public LeaderBoardSync(String category, int size){
        this.category = category;
        this.size = size;
    }
    public void load(){
        BoardSync d = new BoardSync("daily",category,size,this.comparator);
        d.dataStore(this.dataStore);
        BoardView dv = new BoardView(d,listener,this.comparator);
        views[0]=dv;
        d.load(listener);
        boards[0]=d;
        BoardSync w =new BoardSync("weekly",category,size,this.comparator);
        w.dataStore(this.dataStore);
        BoardView wv = new BoardView(w,listener,this.comparator);
        views[1]=wv;
        w.load(listener);
        boards[1]=w;
        BoardSync t =new BoardSync("total",category,size,this.comparator);
        t.dataStore(this.dataStore);
        BoardView tv = new BoardView(t,listener,this.comparator);
        views[2]=tv;
        t.load(listener);
        boards[2]=t;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public String category() {
        return category;
    }

    public void onView(Entry entry){//build global list
        if(entry.classifier().equals("daily")){
            views[0].onView(entry);
        }
        else if(entry.classifier().equals("weekly")){
            views[1].onView(entry);
        }
        else if(entry.classifier().equals("total")){
            views[2].onView(entry);
        }
    }
    public void registerListener(Listener listener){
        this.listener = listener;
    }
    public Board daily(){
        return views[0];
    }
    public Board weekly(){
        return views[1];
    }
    public Board total(){
        return views[2];
    }
    public void dataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    public void reset(){
        //reset daily, weekly 
    }
}

package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;

/**
 * Updated 8/24/19
 */
public class TopListLeaderBoard implements LeaderBoard {


    private String category;
    private int size;

    private BoardImpl[] boards = new BoardImpl[3];

    private EntryComparator comparator = new EntryComparator();


    private Listener listener;
    private DataStore dataStore;
    public TopListLeaderBoard(String category,int size){
        this.category = category;
        this.size = size;
    }
    public void load(){
        BoardImpl d = new BoardImpl("daily",category,size,this.comparator);
        d.dataStore(this.dataStore);
        d.load();
        boards[0]=d;
        BoardImpl w =new BoardImpl("weekly",category,size,this.comparator);
        w.dataStore(this.dataStore);
        w.load();
        boards[1]=w;
        BoardImpl t =new BoardImpl("total",category,size,this.comparator);
        t.dataStore(this.dataStore);
        t.load();
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

    public void onBoard(Entry entry){
        if(entry.classifier().equals("daily")){

        }
        else if(entry.classifier().equals("weekly")){

        }
        else if(entry.classifier().equals("total")){

        }
    }
    public void onBoard(String systemId,double value) {
        for(int i=0;i<3;i++){
            boards[i].onBoard(new EntryImpl(systemId,value,System.currentTimeMillis()),listener);
        }
    }
    public void registerListener(Listener listener){
        this.listener = listener;
    }
    public Board daily(){
        return boards[0];
    }
    public Board weekly(){
        return boards[1];
    }
    public Board total(){
        return boards[2];
    }

    public void reset(){

    }

    @Override
    public void dataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void update() {

    }
}

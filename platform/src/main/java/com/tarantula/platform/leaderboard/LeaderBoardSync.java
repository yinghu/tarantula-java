package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;

import java.util.List;

/**
 * Updated 8/24/19
 */
public class LeaderBoardSync implements LeaderBoard {


    private String category;
    private int size;

    private BoardImpl[] boards = new BoardImpl[3];


    private EntryComparator comparator = new EntryComparator();
    private Listener listener;
    private DataStore dataStore;

    public LeaderBoardSync(String category, int size){
        this.category = category;
        this.size = size;
    }
    public void load(){
        BoardImpl d = new BoardImpl("daily",category,size,this.comparator);
        d.dataStore(this.dataStore);
        d.load(listener);
        boards[0]=d;
        BoardImpl w =new BoardImpl("weekly",category,size,this.comparator);
        w.dataStore(this.dataStore);
        w.load(listener);
        boards[1]=w;
        BoardImpl t =new BoardImpl("total",category,size,this.comparator);
        t.dataStore(this.dataStore);
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

    public void onBoard(Entry entry){//build global list
        System.out.println(entry.toString());
        if(entry.classifier().equals("daily")){

        }
        else if(entry.classifier().equals("weekly")){

        }
        else if(entry.classifier().equals("total")){

        }
    }
    public void registerListener(Listener listener){
        this.listener = listener;
    }
    public Board daily(){
        return new _Board(boards[0],listener);
    }
    public Board weekly(){
        return new _Board(boards[1],listener);
    }
    public Board total(){
        return new _Board(boards[2],listener);
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
    static class _Board implements Board{
        private final BoardImpl impl;
        private final Listener listener;
        public _Board(BoardImpl board,Listener listener){
            this.impl = board;
            this.listener = listener;
        }
        @Override
        public void onBoard(String systemId, double value) {
            impl.onBoard(new EntryImpl(systemId,value,System.currentTimeMillis()),listener);
        }

        @Override
        public List<Entry> list() {
            //return global list from listener
            return impl.list();
        }

        @Override
        public void dataStore(DataStore dataStore) {

        }

        @Override
        public void update() {

        }
    }
}

package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private SortedSet<Entry> daily = new TreeSet<>(comparator);
    private SortedSet<Entry> weekly = new TreeSet<>(comparator);
    private SortedSet<Entry> total = new TreeSet<>(comparator);


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
        //System.out.println(entry.key().asString()+"<><><><>"+entry.toString());
        if(entry.classifier().equals("daily")){
            daily.add(entry);
        }
        else if(entry.classifier().equals("weekly")){
            weekly.add(entry);
        }
        else if(entry.classifier().equals("total")){
            total.add(entry);
        }
    }
    public void registerListener(Listener listener){
        this.listener = listener;
    }
    public Board daily(){
        return new _Board(boards[0],listener,daily);
    }
    public Board weekly(){
        return new _Board(boards[1],listener,weekly);
    }
    public Board total(){
        return new _Board(boards[2],listener,total);
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
        private final SortedSet<Entry> list;
        public _Board(BoardImpl board,Listener listener,SortedSet list){
            this.impl = board;
            this.listener = listener;
            this.list = list;
        }
        @Override
        public void onBoard(String systemId, double value) {
            impl.onBoard(new EntryImpl(systemId,value,System.currentTimeMillis()),listener);
        }

        @Override
        public List<Entry> list() {
            //return global list from listener
            list.forEach((e)->{
                System.out.println(e.toString());
            });
            return new ArrayList<>();
        }

        @Override
        public void dataStore(DataStore dataStore) {

        }

        @Override
        public void update() {

        }
    }
}

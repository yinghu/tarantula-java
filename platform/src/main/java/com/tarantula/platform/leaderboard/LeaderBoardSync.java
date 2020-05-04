package com.tarantula.platform.leaderboard;

import com.tarantula.DataStore;
import com.tarantula.LeaderBoard;
import com.tarantula.Statistics;

import java.time.LocalDate;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Updated 5/4/2020
 */
public class LeaderBoardSync implements LeaderBoard{


    private String category;
    private int size;

    private BoardSync[] boards = new BoardSync[5];

    private EntryComparator comparator = new EntryComparator();
    private Listener listener;
    private DataStore dataStore;

    private BoardView[] views = new BoardView[5];

    private CopyOnWriteArraySet<Listener> listeners;

    public LeaderBoardSync(String category, int size){
        this.category = category;
        this.size = size;
        listeners = new CopyOnWriteArraySet<>();
    }
    public void load(){
        //daily
        BoardSync d = new BoardSync(DAILY,category,size,this.comparator);
        d.dataStore(this.dataStore);
        BoardView dv = new BoardView(d,listener,this.comparator);
        views[0]=dv;
        d.load(listener);
        boards[0]=d;
        //weekly
        BoardSync w =new BoardSync(WEEKLY,category,size,this.comparator);
        w.dataStore(this.dataStore);
        BoardView wv = new BoardView(w,listener,this.comparator);
        views[1]=wv;
        w.load(listener);
        boards[1]=w;
        //monthly
        BoardSync m =new BoardSync(MONTHLY,category,size,this.comparator);
        m.dataStore(this.dataStore);
        BoardView mv = new BoardView(m,listener,this.comparator);
        views[2]=mv;
        m.load(listener);
        boards[2]=m;
        //yearly
        BoardSync y =new BoardSync(YEARLY,category,size,this.comparator);
        y.dataStore(this.dataStore);
        BoardView yv = new BoardView(y,listener,this.comparator);
        views[3]=yv;
        y.load(listener);
        boards[3]=y;
        //total
        BoardSync t =new BoardSync(TOTAL,category,size,this.comparator);
        t.dataStore(this.dataStore);
        BoardView tv = new BoardView(t,listener,this.comparator);
        views[4]=tv;
        t.load(listener);
        boards[4]=t;
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public String category() {
        return category;
    }
    @Override
    public void onAllBoard(Statistics.Entry entry){
        boards[0].onBoard(new LeaderBoardEntry(entry.distributionKey(),entry.daily(),System.currentTimeMillis()),this.listener);
        boards[1].onBoard(new LeaderBoardEntry(entry.distributionKey(),entry.weekly(),System.currentTimeMillis()),this.listener);
        boards[2].onBoard(new LeaderBoardEntry(entry.distributionKey(),entry.monthly(),System.currentTimeMillis()),this.listener);
        boards[3].onBoard(new LeaderBoardEntry(entry.distributionKey(),entry.yearly(),System.currentTimeMillis()),this.listener);
        boards[4].onBoard(new LeaderBoardEntry(entry.distributionKey(),entry.total(),System.currentTimeMillis()),this.listener);
    }
    public void onView(Entry entry){//build global list
        boolean _boarding = false;
        if(entry.classifier().equals(DAILY)){
            _boarding = views[0].onView(entry);
        }
        else if(entry.classifier().equals(WEEKLY)){
            _boarding = views[1].onView(entry);
        }
        else if(entry.classifier().equals(MONTHLY)){
            _boarding = views[2].onView(entry);
        }
        else if(entry.classifier().equals(YEARLY)){
            _boarding = views[3].onView(entry);
        }
        else if(entry.classifier().equals(TOTAL)){
            _boarding = views[4].onView(entry);
        }
        if(_boarding){
            this.listeners.forEach((l)->l.onUpdated(entry));
        }
    }
    public void masterListener(Listener listener){
        this.listener = listener;
    }
    public void addListener(Listener listener){
        this.listeners.add(listener);
    }
    public Board daily(){
        return views[0];
    }
    public Board weekly(){
        return views[1];
    }
    public Board monthly(){
        return views[2];
    }
    public Board yearly(){
        return views[3];
    }
    public Board total(){
        return views[4];
    }
    public void dataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    public void reset(){
        LocalDate localDate = LocalDate.now();
        views[0].reset();
        if (localDate.getDayOfWeek().getValue()==1){
            views[1].reset();
        }
        if(localDate.getDayOfMonth()==1){
            views[2].reset();
        }
        if(localDate.getDayOfYear()==1){
            views[3].reset();
        }
    }
}

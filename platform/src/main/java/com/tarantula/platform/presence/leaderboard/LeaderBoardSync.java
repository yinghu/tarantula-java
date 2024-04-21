package com.tarantula.platform.presence.leaderboard;

import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Statistics;

import java.time.LocalDate;


public class LeaderBoardSync implements LeaderBoard {

    private long gameClusterId;
    private String category;
    private int size;

    private EntryComparator comparator = new EntryComparator();
    private Listener listener;
    private DataStore dataStore;

    private BoardView[] views = new BoardView[5];


    public LeaderBoardSync(String category, int size,long gameClusterId,Listener listener){
        this.category = category;
        this.size = size;
        this.gameClusterId = gameClusterId;
        this.listener = listener;

    }
    public void load(){
        //daily
        BoardSync d = new BoardSync(DAILY,category,size,this.comparator);
        d.distributionId(gameClusterId);
        d.dataStore(this.dataStore);
        BoardView dv = new BoardView(d,listener,this.comparator);
        views[0]=dv;
        dv.load();

        //weekly
        BoardSync w =new BoardSync(WEEKLY,category,size,this.comparator);
        w.distributionId(gameClusterId);
        w.dataStore(this.dataStore);
        BoardView wv = new BoardView(w,listener,this.comparator);
        views[1]=wv;
        wv.load();

        //monthly
        BoardSync m =new BoardSync(MONTHLY,category,size,this.comparator);
        m.distributionId(gameClusterId);
        m.dataStore(this.dataStore);
        BoardView mv = new BoardView(m,listener,this.comparator);
        views[2]=mv;
        mv.load();

        //yearly
        BoardSync y =new BoardSync(YEARLY,category,size,this.comparator);
        y.distributionId(gameClusterId);
        y.dataStore(this.dataStore);
        BoardView yv = new BoardView(y,listener,this.comparator);
        views[3]=yv;
        yv.load();

        //total
        BoardSync t =new BoardSync(TOTAL,category,size,this.comparator);
        t.distributionId(gameClusterId);
        t.dataStore(this.dataStore);
        BoardView tv = new BoardView(t,listener,this.comparator);
        views[4]=tv;
        tv.load();

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
        views[0].onBoard(entry.systemId(),entry.daily());
        views[1].onBoard(entry.systemId(),entry.weekly());
        views[2].onBoard(entry.systemId(),entry.monthly());
        views[3].onBoard(entry.systemId(),entry.yearly());
        views[4].onBoard(entry.systemId(),entry.total());
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

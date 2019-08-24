package com.tarantula.platform.leaderboard;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Updated by yinghu lu on 8/24/2019.
 */
public class Top10LeaderBoardServiceProvider implements LeaderBoardServiceProvider,SchedulingTask {

    private static TarantulaLogger log = JDKLogger.getLogger(Top10LeaderBoardServiceProvider.class);

    private DataStore dataStore;
    private int size = 10;

    private ConcurrentHashMap<String,Top10LeaderBoard> vMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,Top10LeaderBoard> lMap = new ConcurrentHashMap<>();


    private AtomicLong taskDelay;

    private ServiceContext serviceContext;
    public LeaderBoard leaderBoard(String header, String category, String classifier){
        return load(header,category,classifier);
    }
    public void onLeaderBoard(String systemId,LeaderBoard.Entry[] entries){
        for(LeaderBoard.Entry e : entries){
            LeaderBoard ldb = load(e.header(),e.category(),e.classifier());
            ldb.onBoard(systemId,e);
            for(LeaderBoard.Entry ex : ldb.list()){
                log.info(ex.toString());
            }
        }
    }
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public String name() {
        return "TOP10";
    }

    @Override
    public void setup(ServiceContext serviceContext){
        this.dataStore = serviceContext.dataStore("leaderBoard",serviceContext.partitionNumber());
        this.serviceContext = serviceContext;
        taskDelay = new AtomicLong(SystemUtil.toMidnight());
        this.serviceContext.schedule(this);
        log.info("Top 10 leader board service provider started");
    }

    @Override
    public void waitForData() {

    }

    private LeaderBoard load(String h,String c,String a){
        String k = new StringBuffer("top10").append(Recoverable.PATH_SEPARATOR).append(h).append(Recoverable.PATH_SEPARATOR).append(c).append(Recoverable.PATH_SEPARATOR).append(a).toString();
        return lMap.computeIfAbsent(k,(kc)->{
           Top10LeaderBoard top = new Top10LeaderBoard("top10",h,c,a,size,this.dataStore);
           if(a.equals(LeaderBoard.DAILY)){
               top.registerReset(new DailyReset());
           }
           else if(a.equals(LeaderBoard.WEEKLY)){
                top.registerReset(new WeeklyReset());
           }
           else if(a.equals(LeaderBoard.MONTHLY)){
                top.registerReset(new MonthlyReset());
           }
           else if(a.equals(LeaderBoard.YEARLY)){
               top.registerReset(new YearlyReset());
           }
           else if(a.equals(LeaderBoard.TOTAL)){
               top.registerReset(new TotalReset());
           }
           if(dataStore.createIfAbsent(top,true)){
               for(int i=0;i<size;i++){
                    LeaderBoardEntry e = new LeaderBoardEntry();
                    e.owner(top.key().asString());
                    e.onEdge(true);
                    dataStore.create(e);
                    top.entry(i,e);
               }
           }
           else{
               LeaderBoardEntryQuery lq = new LeaderBoardEntryQuery(top.key().asString());
               int[] ix={0};
               this.dataStore.list(lq,(e)->{
                   top.entry(ix[0],e);
                   ix[0]++;
                   log.info(">>>>>>>>>>>>>>>>>>>>>>"+e.toString());
                   return true;
               });
           }
           top.reset();
           return top;
        });
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return taskDelay.get();
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        vMap.forEach((k,v)->{
            v.reset();
        });
        lMap.forEach((k,v)->{
            v.reset();
        });
        taskDelay.set(SystemUtil.toMidnight());
        serviceContext.schedule(this);
    }
}

package com.tarantula.platform.leaderboard;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
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

    private ConcurrentHashMap<String,LeaderBoardRegistry> rMap = new ConcurrentHashMap<>();

    private LeaderBoardQuery query;
    private AtomicLong taskDelay;

    private ServiceContext serviceContext;
    public LeaderBoard leaderBoard(String header, String category, String classifier){
        return view(header,category,classifier);
    }
    public void onLeaderBoard(String systemId,LeaderBoard.Entry[] entries){
        for(LeaderBoard.Entry e : entries){
            LeaderBoard ldb = load(e.header(),e.category(),e.classifier());
            ldb.onBoard(systemId,e);
            log.info(ldb.toString()+"/"+ldb.distributionKey());
        }
    }
    public List<LeaderBoard.Registry> onRegistry(){
        ArrayList<LeaderBoard.Registry> rlist = new ArrayList<>();
        rMap.forEach((k,v)->{
            rlist.add(v);
        });
        return rlist;
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
        this.query = new LeaderBoardQuery(this.dataStore.bucket());
        this.serviceContext = serviceContext;
        taskDelay = new AtomicLong(SystemUtil.toMidnight());
        this.serviceContext.schedule(this);
        log.info("Top leader board started");
    }

    @Override
    public void waitForData() {
        log.info(">>>>>>>>>>>>>>>>>>>>> wait for data");
        this.dataStore.list(new LeaderBoardQuery(query.distributionKey()),(l)->{
            String k = new StringBuffer(l.name()).append(Recoverable.PATH_SEPARATOR).append(l.leaderBoardHeader()).append(Recoverable.PATH_SEPARATOR).append(l.category()).append(Recoverable.PATH_SEPARATOR).append(l.classifier()).toString();
            int[] ix = {0};
            LeaderBoard vp = view(l.leaderBoardHeader(),l.category(),l.classifier());
            if(l.classifier().equals(LeaderBoard.DAILY)){
                l.registerReset(new DailyReset());
            }
            else if(l.classifier().equals(LeaderBoard.WEEKLY)){
                l.registerReset(new WeeklyReset());
            }
            else if(l.classifier().equals(LeaderBoard.MONTHLY)){
                l.registerReset(new MonthlyReset());
            }
            else if(l.classifier().equals(LeaderBoard.YEARLY)){
                l.registerReset(new YearlyReset());
            }
            else if(l.classifier().equals(LeaderBoard.TOTAL)){
                l.registerReset(new TotalReset());
            }
            l.preload(dataStore);
            log.info(">>>>>>>>>>>>>>>>>>>>>"+l.toString());
            dataStore.list(new LeaderBoardEntryQuery(l.distributionKey()),(e)->{
                l.entry(ix[0],e);
                ix[0]++;
                log.info(e.toString());
                if(!e.systemId().equals("--")){
                    vp.onBoard(e.systemId(),e);
                }
                return true;
            });
            lMap.put(k,l);
            return true;
        });
    }

    private LeaderBoard view(String h,String c,String a){
        String k = new StringBuffer("top10").append(Recoverable.PATH_SEPARATOR).append(h).append(Recoverable.PATH_SEPARATOR).append(c).append(Recoverable.PATH_SEPARATOR).append(a).toString();
        return vMap.computeIfAbsent(k,(kc)->{
            //top/presence/weekly/loginCount
            Top10LeaderBoard v = new Top10LeaderBoard("top10",h,c,a,size,null);//pass null data store without update
            if(a.equals(LeaderBoard.DAILY)){
                v.registerReset(new DailyReset());
            }
            else if(a.equals(LeaderBoard.WEEKLY)){
                v.registerReset(new WeeklyReset());
            }
            else if(a.equals(LeaderBoard.MONTHLY)){
                v.registerReset(new MonthlyReset());
            }
            else if(a.equals(LeaderBoard.YEARLY)){
                v.registerReset(new YearlyReset());
            }
            else if(a.equals(LeaderBoard.TOTAL)){
                v.registerReset(new TotalReset());
            }
            for(int i=0;i<size;i++){
                v.entry(i,new LeaderBoardEntry());
            }
            LeaderBoardRegistry r = registry(h);
            r.categoryList.add(c);
            r.classifierList.add(a);
            return v;
        });
    }
    private LeaderBoardRegistry registry(String header){
        return rMap.computeIfAbsent(header,(h)-> new LeaderBoardRegistry("top10",header,size));
    }
    private LeaderBoard load(String h,String c,String a){
        String k = new StringBuffer("top10").append(Recoverable.PATH_SEPARATOR).append(h).append(Recoverable.PATH_SEPARATOR).append(c).append(Recoverable.PATH_SEPARATOR).append(a).toString();
        return lMap.computeIfAbsent(k,(kc)->{
           Top10LeaderBoard top = new Top10LeaderBoard("top10",h,c,a,size,this.dataStore);
           top.owner(query.distributionKey());
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
           if(dataStore.create(top)){
               for(int i=0;i<size;i++){
                    LeaderBoardEntry e = new LeaderBoardEntry(k);
                    e.owner(top.distributionKey());
                    e.onEdge(true);
                    dataStore.create(e);
                    top.entry(i,e);
               }
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

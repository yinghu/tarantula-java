package com.tarantula.platform.leaderboard;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;
import com.tarantula.platform.service.LeaderBoardServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Updated by yinghu lu on 8/24/2019.
 */
public class Top10LeaderBoardServiceProvider implements LeaderBoardServiceProvider,SchedulingTask,EventListener {

    private static TarantulaLogger log = JDKLogger.getLogger(Top10LeaderBoardServiceProvider.class);

    private static String _NAME = "top10";

    private DataStore dataStore;
    private int size = 10;

    //global board view
    private ConcurrentHashMap<String, TopListLeaderBoard> vMap = new ConcurrentHashMap<>();

    //local board view
    private ConcurrentHashMap<String, TopListLeaderBoard> lMap = new ConcurrentHashMap<>();

    private AtomicLong taskDelay;

    private ServiceContext serviceContext;
    private EventService eventService;
    public LeaderBoard leaderBoard(String h, String c,String a){
        String k = new StringBuffer(_NAME).append(Recoverable.PATH_SEPARATOR).append(h).append(Recoverable.PATH_SEPARATOR).append(c).append(Recoverable.PATH_SEPARATOR).append(a).toString();
        return vMap.get(k);
    }
    public void onLeaderBoard(String systemId,LeaderBoard.Entry[] entries){
        for(LeaderBoard.Entry e : entries){
            LeaderBoard ldb = load(e.header(),e.category(),e.classifier());
            if(ldb.onBoard(systemId,e)){
                e.update(systemId,e.value(),e.timestamp());
                this.eventService.publish(new LeaderBoardGlobalEvent(_NAME,e));
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
        return _NAME;
    }
    public boolean onEvent(Event event){
        LeaderBoardEntry entry = new LeaderBoardEntry(event.applicationId(),event.instanceId(),event.clientId(),event.balance(),event.timestamp());
        LeaderBoard gdb = vMap.get(_toKey(event.applicationId(),event.instanceId(),event.clientId()));
        gdb.onBoard(event.systemId(),entry);
        return false;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        this.dataStore = serviceContext.dataStore("leaderBoard",serviceContext.partitionNumber());
        this.serviceContext = serviceContext;
        this.eventService = this.serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).subscribe(_NAME,this);
        taskDelay = new AtomicLong(SystemUtil.toMidnight());
        this.serviceContext.schedule(this);
        log.info("Top 10 leader board service provider started");
    }

    @Override
    public void waitForData() {

    }
    private String _toKey(String h,String c,String a){
        return new StringBuffer(_NAME).append(Recoverable.PATH_SEPARATOR).append(h).append(Recoverable.PATH_SEPARATOR).append(c).append(Recoverable.PATH_SEPARATOR).append(a).toString();
    }
    private LeaderBoard load(String h,String c,String a){
        String k = _toKey(h,c,a);
        return lMap.computeIfAbsent(k,(kc)->{
            TopListLeaderBoard top = new TopListLeaderBoard(_NAME,h,c,a,size,this.dataStore,true);
            TopListLeaderBoard gview = new TopListLeaderBoard(_NAME,h,c,a,size,this.dataStore,false);
            if(a.equals(LeaderBoard.DAILY)){
               top.registerReset(new DailyReset());
               gview.registerReset(new DailyReset());
           }
           else if(a.equals(LeaderBoard.WEEKLY)){
                top.registerReset(new WeeklyReset());
                gview.registerReset(new WeeklyReset());
           }
           else if(a.equals(LeaderBoard.MONTHLY)){
                top.registerReset(new MonthlyReset());
                gview.registerReset(new MonthlyReset());
           }
           else if(a.equals(LeaderBoard.YEARLY)){
               top.registerReset(new YearlyReset());
               gview.registerReset(new YearlyReset());
           }
           else if(a.equals(LeaderBoard.TOTAL)){
               top.registerReset(new TotalReset());
               gview.registerReset(new TotalReset());
           }
           for(int i=0;i<size;i++){
               gview.entry(i,new LeaderBoardEntry());
           }
           gview.reset();
           vMap.put(k,gview);
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
                   if(!e.systemId().equals("--")){
                       gview.onBoard(e.systemId(),e);
                   }
                   ix[0]++;
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

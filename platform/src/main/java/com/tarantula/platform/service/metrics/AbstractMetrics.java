package com.tarantula.platform.service.metrics;

import com.icodesoftware.*;
import com.icodesoftware.protocol.statistics.StatisticsUtil;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


abstract public class AbstractMetrics implements Metrics, SchedulingTask {


    //GAME CATEGORY
    public final static String GAME_JOIN_COUNT = "joinCount";
    public final static String GAME_TIMEOUT_COUNT = "timeoutCount";


    //ACCESS CATEGORY
    public final static String ACCESS_GOOGLE_LOGIN_COUNT = "googleLoginCount";
    public final static String ACCESS_WEB_LOGIN_COUNT = "webLoginCount";
    public final static String ACCESS_DEVICE_LOGIN_COUNT = "deviceLoginCount";
    public final static String ACCESS_FACEBOOK_LOGIN_COUNT = "facebookLoginCount";
    public final static String ACCESS_GAME_CENTER_LOGIN_COUNT = "gameCenterLoginCount";
    public final static String ACCESS_DEVELOPER_LOGIN_COUNT = "developerLoginCount";
    public final static String ACCESS_AMAZON_S3_COUNT = "amazonS3Count";


    private ConcurrentHashMap<String, StatsDelta> pendingUpdates;

    protected long pendingUpdateInterval = 10000;
    protected int metricsTrackingNumber = Metrics.SNAPSHOT_TRACKING_SIZE;

    protected DataStore dataStore;
    private SystemStatistics statistics;

    private CopyOnWriteArraySet<String> categories;
    protected TarantulaLogger logger;
    protected String name;

    private Lock lock = new ReentrantLock();

    private ConcurrentHashMap<String, MetricsSnapshot> snapshots;
    private ConcurrentHashMap<String,MetricsHistory> archives;


    public String name(){
        return name;
    }

    public void setup(ServiceContext serviceContext){
        this.categories = new CopyOnWriteArraySet<>();
        this.pendingUpdates = new ConcurrentHashMap<>();
        this.snapshots = new ConcurrentHashMap<>();
        this.archives = new ConcurrentHashMap<>();
        _setup(serviceContext);
        long nodeId = serviceContext.node().nodeId();
        LocalDateTime _cur = LocalDateTime.now();
        this.statistics = new SystemStatistics();
        this.statistics.distributionId(nodeId);
        this.statistics.label(name);
        this.statistics.dataStore(this.dataStore);
        this.statistics.load();
        //reset snapshots
        for(Statistics.Entry e : this.statistics.summary()){
            //reset statistics entry and archive history
            String category = e.name();
            registerCategory(category);
            SystemStatisticsEntry entry = (SystemStatisticsEntry) statistics.entry(category);
            LocalDateTime lastUpdate = TimeUtil.fromUTCMilliseconds(entry.timestamp());
            MetricsHistory lastMetricsHistory = metricsHistory(category,lastUpdate);
            if(!StatisticsUtil.validateHourly(lastUpdate,_cur)){
                String xh = MetricsSnapshot.hourlyLabel(lastUpdate);
                Spot property = new MetricsProperty(xh,entry.hourly(),_cur);
                lastMetricsHistory.archiveHourly(property);
                entry.hourly(0,_cur);

                if(!StatisticsUtil.validateDaily(lastUpdate,_cur)){
                    lastMetricsHistory.archiveDaily(entry.daily(),_cur);
                    entry.daily(0,_cur);
                }
                if(!StatisticsUtil.validateWeekly(lastUpdate,_cur)){
                    lastMetricsHistory.archiveWeekly(entry.weekly(),_cur);
                    entry.weekly(0,_cur);
                }
                if(!StatisticsUtil.validateMonthly(lastUpdate,_cur)){
                    lastMetricsHistory.archiveMonthly(entry.monthly(),_cur);
                    entry.monthly(0,_cur);
                }
                if(!StatisticsUtil.validateYearly(lastUpdate,_cur)){
                    lastMetricsHistory.archiveYearly(entry.yearly(),_cur);
                    entry.yearly(0,_cur);
                }
                entry.update();
            }
            //reset snapshot
            MetricsSnapshot hourly = metricsSnapshot(category,LeaderBoard.HOURLY);
            initialize(LeaderBoard.HOURLY,hourly,_cur);
            this.dataStore.update(hourly);

            MetricsSnapshot daily = metricsSnapshot(category,LeaderBoard.DAILY);
            initialize(LeaderBoard.DAILY,daily,_cur);
            this.dataStore.update(daily);

            MetricsSnapshot weekly = metricsSnapshot(category,LeaderBoard.WEEKLY);
            initialize(LeaderBoard.WEEKLY,weekly,_cur);
            this.dataStore.update(weekly);

            MetricsSnapshot monthly = metricsSnapshot(category,LeaderBoard.MONTHLY);
            initialize(LeaderBoard.MONTHLY,monthly,_cur);
            this.dataStore.update(monthly);

            MetricsSnapshot yearly = metricsSnapshot(category,LeaderBoard.YEARLY);
            initialize(LeaderBoard.YEARLY,yearly,_cur);
            this.dataStore.update(yearly);
        }
        serviceContext.schedule(this);
        logger.warn("Metrics ["+name+"] has registered with update rate at ["+(pendingUpdateInterval/1000)+"] seconds");
    }


    @Override
    public void onUpdated(String category, double delta) {
        logger.warn("Metrics : "+name+" : "+category+" : " +delta);
        pendingUpdates.compute(category,(k,v)->{
            if(delta<=0) return v;//ignore
            if(v==null) {
                categories.add(category);
                return new StatsDelta(category,delta);
            }
            v.value += delta;
            return v;
        });
    }

    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return pendingUpdateInterval;
    }

    @Override
    public void run(){
        try {
            lock.tryLock(3, TimeUnit.MILLISECONDS);
            _run();
            lock.unlock();
        }
        catch (Exception ex){
            logger.warn("Give away lock to hourly call",ex);
            //just skip for hourly update
        }
    }
    private void _run() {
        ArrayList<StatsDelta> pendings = new ArrayList<>();
        pendingUpdates.forEach((category,v)->{
            pendings.add(v.reset());
        });
        pendings.forEach(p->{
            try {
                if(p.value>0){
                    Statistics.Entry e = statistics.entry(p.name);
                    e.update(p.value).update();
                    metricsSnapshot(e.name(),LeaderBoard.HOURLY).update(e.hourly()).update();
                    metricsSnapshot(e.name(),LeaderBoard.DAILY).update(e.daily()).update();
                    metricsSnapshot(e.name(),LeaderBoard.WEEKLY).update(e.weekly()).update();
                    metricsSnapshot(e.name(),LeaderBoard.MONTHLY).update(e.monthly()).update();
                    metricsSnapshot(e.name(),LeaderBoard.YEARLY).update(e.yearly()).update();
                }
            }catch (Exception ex){
                //ignore
                logger.error("Error on update",ex);
            }
        });
        pendings.clear();
    }

    @Override
    public Statistics statistics() {
        return statistics;
    }

    public List<String> categories(){
        return new ArrayList<>(categories);
    }
    public Spot[] snapshot(String category, String classifier){
        MetricsSnapshot metricsSnapshot = metricsSnapshot(category,classifier);
        return metricsSnapshot.metrics();
    }
    public History archive(String category,LocalDateTime end){
        return metricsHistory(category,end);
    }
    public History archiveWeekly(String category,LocalDateTime end){
        LocalDateTime wday = TimeUtil.toLastMonday(end);
        return metricsHistory(category,wday);
    }
    public History archiveMonthly(String category,LocalDateTime end){
        LocalDateTime mday = TimeUtil.toFirstDayOfLastMonth(end);
        return metricsHistory(category,mday);
    }
    public History archiveYearly(String category,LocalDateTime end){
        LocalDateTime yday = TimeUtil.toFirstDayOfLastYear(end);
        return metricsHistory(category,yday);
    }


    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.run();
    }

    private void atMidnight(LocalDateTime end){
        categories.forEach(category->{
            SystemStatisticsEntry entry = (SystemStatisticsEntry)statistics.entry(category);
            //daily reset
            double daily = entry.daily();
            MetricsHistory metricsHistory = this.metricsHistory(category,end.minusDays(1));//log to previous day
            metricsHistory.archiveDaily(daily,end);
            entry.daily(0,end);
            MetricsSnapshot md = metricsSnapshot(category,LeaderBoard.DAILY);
            md.update(daily);
            String xd = MetricsSnapshot.dailyLabel(end.plusDays(1));
            MetricsProperty pd = new MetricsProperty(xd,0,end);
            md.push(pd,end);
            this.dataStore.update(md);
            if(end.getDayOfWeek().getValue() == 1){//weekly reset
                double weekly = entry.weekly();
                metricsHistory.archiveWeekly(weekly,end);
                entry.weekly(0,end);
                MetricsSnapshot mw = metricsSnapshot(category,LeaderBoard.WEEKLY);
                mw.update(weekly);
                String xw = MetricsSnapshot.weeklyLabel(end.plusDays(8-end.getDayOfWeek().getValue()));
                MetricsProperty pw = new MetricsProperty(xw,0,end);
                mw.push(pw,end);
                this.dataStore.update(mw);

            }
            if(end.getDayOfMonth() ==1){//monthly reset
                double monthly = entry.monthly();
                metricsHistory.archiveMonthly(monthly,end);
                entry.monthly(0,end);
                MetricsSnapshot mm = metricsSnapshot(category,LeaderBoard.MONTHLY);
                mm.update(monthly);
                String xm = MetricsSnapshot.monthlyLabel(end.plusMonths(1));
                MetricsProperty pm = new MetricsProperty(xm,0,end);
                mm.push(pm,end);
                this.dataStore.update(mm);
            }
            if(end.getDayOfYear() == 1){ //yearly reset
                double yearly = entry.yearly();
                metricsHistory.archiveYearly(yearly,end);
                entry.yearly(0,end);
                MetricsSnapshot my = metricsSnapshot(category,LeaderBoard.MONTHLY);
                my.update(yearly);
                String xy = MetricsSnapshot.yearlyLabel(end.plusYears(1));
                MetricsProperty py = new MetricsProperty(xy,0,end);
                my.push(py,end);
                this.dataStore.update(my);
            }
            this.dataStore.update(metricsHistory);
            entry.update();
        });
    }

    public void atHourly(){
        try {
            lock.lock();
            _run();
            LocalDateTime end = end();
            //logger.warn("Running hourly task at->"+end);
            categories.forEach(category->{
                SystemStatisticsEntry entry = (SystemStatisticsEntry)statistics.entry(category);
                double hourly = entry.hourly();
                MetricsSnapshot snapshot = metricsSnapshot(category,LeaderBoard.HOURLY);
                snapshot.update(hourly);
                String xh = MetricsSnapshot.hourlyLabel(end.plusHours(2));
                MetricsProperty property = new MetricsProperty(xh,0,end);
                Spot history = snapshot.push(property,end);
                //archive history hourly
                MetricsHistory metricsHistory = metricsHistory(category,end);
                metricsHistory.archiveHourly(new MetricsProperty(historyPropertyLabel(end),history.value(),end));
                this.dataStore.update(metricsHistory);
                this.dataStore.update(snapshot);
                //reset hourly metrics
                entry.hourly(0,end);
                entry.update();
            });
            if (end.getHour() == 23) {
                atMidnight(TimeUtil.midnight(end.toLocalDate()));
            }
        }catch (Exception ex){
            //ignore
            logger.warn("error on at hourly",ex);
        }
        finally {
            lock.unlock();
        }

    }
    private String historyPropertyLabel(LocalDateTime current){
        return MetricsProperty.historyPropertyLabel(current);
    }
    private String historyLabel(String category,LocalDateTime today){
        return MetricsHistory.historyLabel(this.statistics.distributionId(),category,today);
    }

    private String categoryKey(String category,String classifier){
        return new StringBuffer().append(category).append("_").append(classifier).toString();
    }
    private void initialize(String classifier,MetricsSnapshot metricsSnapshot,LocalDateTime current){
        LocalDateTime _cur = current;
        int track = metricsTrackingNumber-1;
        switch (classifier){
            case LeaderBoard.HOURLY:
                LocalDateTime hf = _cur.plusHours(1);//to next hour
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xhf = hf.minusHours(track-i);
                    String xh = MetricsSnapshot.hourlyLabel(xhf);
                    metricsSnapshot.initialize(i,new MetricsProperty(xh,0d,xhf),_cur);
                }
                break;
            case LeaderBoard.DAILY:
                LocalDateTime df = _cur.plusDays(1);//to next day
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xdf = df.minusDays(track-i);
                    String xd = MetricsSnapshot.dailyLabel(xdf);
                    metricsSnapshot.initialize(i,new MetricsProperty(xd,0d,xdf),_cur);
                }
                break;
            case LeaderBoard.WEEKLY:
                LocalDateTime wf = _cur.plusDays((8-_cur.getDayOfWeek().getValue()));//toMonday
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xwf = wf.minusWeeks(track-i);
                    String xw = MetricsSnapshot.weeklyLabel(xwf);
                    metricsSnapshot.initialize(i,new MetricsProperty(xw,0d,xwf),_cur);
                }
                break;
            case LeaderBoard.MONTHLY:
                LocalDateTime mf = _cur.minusDays(_cur.getDayOfMonth()-1).plusMonths(1);//toFirstDayOfMonth
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xmf = mf.minusMonths(track-i);
                    String xm = MetricsSnapshot.monthlyLabel(xmf);
                    metricsSnapshot.initialize(i,new MetricsProperty(xm,0d,xmf),_cur);
                }
                break;
            case LeaderBoard.YEARLY:
                LocalDateTime yf = _cur.minusDays(_cur.getDayOfYear()-1).plusYears(1);//toFirstDayOfYear
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xyf = yf.minusYears(track-i);
                    String xd = MetricsSnapshot.yearlyLabel(xyf);
                    metricsSnapshot.initialize(i,new MetricsProperty(xd,0d,xyf),_cur);
                }
                break;
            default:
                logger.warn("classifier not support");
        }
    }
    private MetricsSnapshot metricsSnapshot(String category,String classifier){
        String ckey = categoryKey(category,classifier);
        return snapshots.computeIfAbsent(ckey,k->{
            MetricsSnapshot pending = this.statistics.loadMetricsSnapshot(classifier);
            if(pending!=null) return pending;
            pending = new MetricsSnapshot(category,classifier);
            pending.ownerKey(this.statistics.key());
            initialize(classifier,pending,LocalDateTime.now());
            pending.dataStore(this.dataStore);
            this.dataStore.create(pending);
            return pending;
        });
    }
    private MetricsHistory metricsHistory(String category,LocalDateTime end){
        String akey = historyLabel(category,end);
        return archives.computeIfAbsent(akey,k->{
            MetricsHistory metricsHistory = this.statistics.loadMetricsHistory(end.getDayOfYear());
            if(metricsHistory!=null) return metricsHistory;
            metricsHistory = new MetricsHistory(category,end.getYear(),end.getDayOfYear());
            metricsHistory.ownerKey(this.statistics.key());
            metricsHistory.initializeHourly(end);
            metricsHistory.dataStore(dataStore);
            this.dataStore.create(metricsHistory);
            return metricsHistory;
        });
    }
    /**
     * set data store
     * set categories
     * set logger
     * set pendingUpdateInterval
     * */
    abstract void _setup(ServiceContext serviceContext);

    //unit test date time injection
    protected LocalDateTime end(){
        return LocalDateTime.now();
    }

    protected void registerCategory(String category){
        if(pendingUpdates.containsKey(category)) return;
        categories.add(category);
        pendingUpdates.put(category,new StatsDelta(category,0));
    }
    @Override
    public void registerSummary(Summary summary){
        categories.forEach(c->summary.registerCategory(c));
    }
    @Override
    public void updateSummary(Summary summary){
        categories.forEach(c->{
            summary.update(c,statistics.entry(c).total());
        });
    }

}

package com.tarantula.platform.service.metrics;

import com.icodesoftware.*;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.statistics.StatsDelta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


abstract public class AbstractMetrics implements Metrics, SchedulingTask, Serviceable {

    //PAYMENT CATEGORY
    public final static String PAYMENT_GOOGLE_STORE_COUNT = "googleStoreCount";
    public final static String PAYMENT_APPLE_STORE_COUNT = "appleStoreCount";
    public final static String PAYMENT_STRIPE_COUNT = "stripeCount";
    public final static String PAYMENT_GOOGLE_STORE_AMOUNT = "googleStoreAmount";
    public final static String PAYMENT_APPLE_STORE_AMOUNT = "appleStoreAmount";
    public final static String PAYMENT_STRIPE_AMOUNT = "stripeAmount";


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

    //PERFORMANCE CATEGORY
    public final static String PERFORMANCE_DATA_STORE_COUNT = "dataStoreCount";
    public final static String PERFORMANCE_CLUSTER_INBOUND_MESSAGE_COUNT = "clusterInboundMessageCount";
    public final static String PERFORMANCE_CLUSTER_OUTBOUND_MESSAGE_COUNT = "clusterOutboundMessageCount";
    public final static String PERFORMANCE_HTTP_REQUEST_COUNT = "httpRequestCount";
    public final static String PERFORMANCE_UDP_REQUEST_COUNT = "udpRequestCount";

    //DEPLOYMENT CATEGORY
    public final static String DEPLOYMENT_GAME_CLUSTER_COUNT = "gameClusterCount";
    public final static String DEPLOYMENT_MESSAGE_RECEIVER_COUNT = "messageReceiverCount";
    public final static String DEPLOYMENT_APPLICATION_COUNT = "applicationCount";

    //ACCOUNT CATEGORY
    public final static String ACCOUNT_USER_CREATION_COUNT = "userCreationCount";
    public final static String ACCOUNT_ACCOUNT_CREATION_COUNT = "accountCreationCount";

    public final static String ACCOUNT_SUBSCRIPTION_COUNT = "subscriptionCount";

    private ConcurrentHashMap<String, StatsDelta> pendingUpdates;

    protected long pendingUpdateInterval = 10000;
    protected int metricsTrackingNumber = 12;

    protected DataStore dataStore;
    private SystemStatistics statistics;
    private ServiceContext serviceContext;
    private ArrayList<String> categories;
    protected TarantulaLogger logger;
    protected String name;

    protected String bucket;
    protected String oid;

    protected boolean paymentIncluded;
    protected boolean accessIncluded;
    protected boolean performanceIncluded;
    protected boolean deploymentIncluded;
    protected boolean accountIncluded;
    protected boolean gameIncluded;

    private Lock lock = new ReentrantLock();

    private ConcurrentHashMap<String, MetricsSnapshot> snapshots;
    private ConcurrentHashMap<String,MetricsHistory> archives;


    public String name(){
        return name;
    }

    public void setup(ServiceContext serviceContext){
        this.categories = new ArrayList<>();
        this.pendingUpdates = new ConcurrentHashMap<>();
        this.snapshots = new ConcurrentHashMap<>();
        this.archives = new ConcurrentHashMap<>();
        _setup(serviceContext);
        //register default categories
        if(paymentIncluded) {
            registerCategory(PAYMENT_GOOGLE_STORE_COUNT);
            registerCategory(PAYMENT_APPLE_STORE_COUNT);
            registerCategory(PAYMENT_STRIPE_COUNT);
            registerCategory(PAYMENT_GOOGLE_STORE_AMOUNT);
            registerCategory(PAYMENT_APPLE_STORE_AMOUNT);
            registerCategory(PAYMENT_STRIPE_AMOUNT);
        }

        if(accessIncluded) {
            registerCategory(ACCESS_GOOGLE_LOGIN_COUNT);
            registerCategory(ACCESS_WEB_LOGIN_COUNT);
            registerCategory(ACCESS_DEVICE_LOGIN_COUNT);
            registerCategory(ACCESS_FACEBOOK_LOGIN_COUNT);
            registerCategory(ACCESS_GAME_CENTER_LOGIN_COUNT);
            registerCategory(ACCESS_DEVELOPER_LOGIN_COUNT);
            registerCategory(ACCESS_AMAZON_S3_COUNT);
        }

        if(performanceIncluded){
            registerCategory(PERFORMANCE_DATA_STORE_COUNT);
            registerCategory(PERFORMANCE_CLUSTER_INBOUND_MESSAGE_COUNT);
            registerCategory(PERFORMANCE_CLUSTER_OUTBOUND_MESSAGE_COUNT);
            registerCategory(PERFORMANCE_HTTP_REQUEST_COUNT);
            registerCategory(PERFORMANCE_UDP_REQUEST_COUNT);
        }

        if(deploymentIncluded){
            registerCategory(DEPLOYMENT_GAME_CLUSTER_COUNT);
            registerCategory(DEPLOYMENT_MESSAGE_RECEIVER_COUNT);
            registerCategory(DEPLOYMENT_APPLICATION_COUNT);
        }

        if(accountIncluded){
            registerCategory(ACCOUNT_USER_CREATION_COUNT);
            registerCategory(ACCOUNT_ACCOUNT_CREATION_COUNT);
            registerCategory(ACCOUNT_SUBSCRIPTION_COUNT);
        }

        if(gameIncluded) {
            registerCategory(GAME_JOIN_COUNT);
            registerCategory(GAME_TIMEOUT_COUNT);
        }
        this.serviceContext = serviceContext;
        String nodeId = serviceContext.nodeId();
        LocalDateTime _cur = LocalDateTime.now();
        String dayAndYear = labelDayAndYear(SystemStatistics.LABEL_PREFIX,_cur);
        this.statistics = new SystemStatistics();
        this.statistics.distributionKey(nodeId+Recoverable.PATH_SEPARATOR+dayAndYear);
        this.statistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(statistics,true);
        this.bucket = this.statistics.bucket();
        this.oid = this.statistics.oid();
        logger.warn("Metrics statistics loaded->"+statistics.key().asString());
        //reset snapshots
        for(String category : categories){
            MetricsSnapshot snapshot = metricsSnapshot(category,LeaderBoard.HOURLY);
            if(!snapshot.validate(_cur)){
                logger.warn("Snapshot is going to reset ["+category+"]");
                Property[] archived = snapshot.metrics();
                for(Property p : archived) {
                    LocalDateTime updated = TimeUtil.fromUTCMilliseconds(p.timestamp());
                    MetricsHistory metricsHistory = metricsHistory(category, LeaderBoard.HOURLY,updated, MetricsHistory.HOURLY_HISTORY_BUFFER_SIZE);
                    metricsHistory.archiveHourly(new MetricsProperty(metricsTrackingNumber-1,historyPropertyLabel(updated),p.value(),updated));
                }
                initialize(LeaderBoard.HOURLY,snapshot,_cur);
            }
            metricsHistory(category,LeaderBoard.HOURLY,_cur,MetricsHistory.HOURLY_HISTORY_BUFFER_SIZE);
        }
        serviceContext.schedule(this);
        logger.warn("Metrics ["+name+"] has registered with update rate at ["+(pendingUpdateInterval/1000)+"] seconds");
    }


    @Override
    public void onUpdated(String s, double delta) {
        pendingUpdates.compute(s,(k,v)->{
            if(v==null || delta<=0) return v;//ignore
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
            logger.warn("Give away lock to hourly call");
            //just skip for hourly update
        }
    }
    private void _run() {
        ArrayList<StatsDelta> pendings = new ArrayList<>();
        for(String category : categories){
            pendingUpdates.compute(category,(k,v)->{
                pendings.add(v.reset());
                return v;
            });
        }
        pendings.forEach(p->{
            try {
                if(p.value>0){
                    Statistics.Entry e = statistics.entry(p.name);
                    e.update(p.value).update();
                    //logger.warn(e.key().asString());
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
        return categories;
    }
    public Property[] snapshot(String category, String classifier){
        MetricsSnapshot metricsSnapshot = metricsSnapshot(category,classifier);
        return metricsSnapshot.metrics();
    }
    public Property[] history(String category, String classifier,LocalDateTime start,LocalDateTime end){
        MetricsHistory metricsHistory = metricsHistory(category,classifier,start,MetricsHistory.HOURLY_HISTORY_BUFFER_SIZE);
        return metricsHistory.metrics();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.run();
    }

    private void atMidnight(LocalDateTime end){
        SystemStatistics next = new SystemStatistics();
        next.distributionKey(this.serviceContext.nodeId()+Recoverable.PATH_SEPARATOR+labelDayAndYear(SystemStatistics.LABEL_PREFIX,end));
        next.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(next,true);
        categories.forEach(category->{
            SystemStatisticsEntry entry = (SystemStatisticsEntry)next.entry(category);
            double daily = statistics.entry(category).daily();
            MetricsSnapshot md = metricsSnapshot(category,LeaderBoard.DAILY);
            md.update(daily);
            String xd = end.plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            Property pd = new MetricsProperty(metricsTrackingNumber-1,xd,0,end.plusDays(1));
            md.push(pd,end);
            this.dataStore.update(md);

            if(end.getDayOfWeek().getValue() != 7){//weekly
                entry.weekly(statistics.entry(category).weekly(),end);
            }
            else if(end.getDayOfWeek().getValue() == 7){
                double weekly = statistics.entry(category).weekly();
                MetricsSnapshot mw = metricsSnapshot(category,LeaderBoard.WEEKLY);
                mw.update(weekly);
                String xw = end.plusDays(7).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                Property pw = new MetricsProperty(metricsTrackingNumber-1,xw,0,end.plusDays(7));
                mw.push(pw,end);
                this.dataStore.update(mw);

            }
            if(end.getDayOfMonth() != 1){//monthly
                entry.monthly(statistics.entry(category).monthly(),end);
            }
            else if(end.getDayOfMonth() ==1){
                double monthly = statistics.entry(category).monthly();
                MetricsSnapshot mm = metricsSnapshot(category,LeaderBoard.MONTHLY);
                mm.update(monthly);
                String xm = end.plusMonths(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                Property pm = new MetricsProperty(metricsTrackingNumber-1,xm,0,end.plusMonths(1));
                mm.push(pm,end);
                this.dataStore.update(mm);
            }
            if(end.getDayOfYear() != 1 ){//yearly
                entry.yearly(statistics.entry(category).yearly(),end);
                entry.total(statistics.entry(category).total(),end);
            }
            else if(end.getDayOfYear() == 1){
                double yearly = statistics.entry(category).yearly();
                MetricsSnapshot my = metricsSnapshot(category,LeaderBoard.MONTHLY);
                my.update(yearly);
                String xy = end.plusYears(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                Property py = new MetricsProperty(metricsTrackingNumber-1,xy,0,end.plusYears(1));
                my.push(py,end);
                this.dataStore.update(my);
            }
            entry.update();
        });
        statistics = next;
    }

    public void atHourly(){
        try {
            lock.lock();
            _run();
            LocalDateTime end = end();
            categories.forEach(category->{
                SystemStatisticsEntry entry = (SystemStatisticsEntry)statistics.entry(category);
                double hourly = entry.hourly();
                MetricsSnapshot snapshot = metricsSnapshot(category,LeaderBoard.HOURLY);
                snapshot.update(hourly);
                LocalDateTime hf = end.minusMinutes(end.getMinute()).plusHours(1);
                String xh = hf.format(DateTimeFormatter.ofPattern("hh:mm a"));
                Property property = new MetricsProperty(metricsTrackingNumber-1,xh,0,hf);
                Property history = snapshot.push(property,end);
                //archive history hourly
                MetricsHistory metricsHistory = metricsHistory(category,LeaderBoard.HOURLY,end,MetricsHistory.HOURLY_HISTORY_BUFFER_SIZE);
                metricsHistory.archiveHourly(new MetricsProperty(metricsTrackingNumber-1,historyPropertyLabel(end),history.value(),end));
                this.dataStore.update(metricsHistory);
                this.dataStore.update(snapshot);
                //reset hourly metrics
                entry.hourly(0,end);
                entry.update();
            });
            if (end.getHour() == 0) {
                atMidnight(end);
            }
        }catch (Exception ex){
            //ignore
            ex.printStackTrace();
        }
        finally {
            lock.unlock();
        }

    }
    private String historyPropertyLabel(LocalDateTime current){
        return new StringBuffer().append(current.getYear()).append(Recoverable.PATH_SEPARATOR).append(current.getDayOfYear()).append(Recoverable.PATH_SEPARATOR).append(current.getHour()).toString();
    }
    private String historyLabel(String category,String classifier,LocalDateTime today){
        StringBuffer buffer = new StringBuffer().append(bucket).append(Recoverable.PATH_SEPARATOR).append(oid);
        buffer.append(Recoverable.PATH_SEPARATOR).append(MetricsHistory.LABEL_PREFIX).append("_").append(category).append("_").append(classifier).append("_");
        int day = today.getDayOfYear();
        int hour = today.getHour();
        int year = today.getYear();
        if(day == 1 && hour==0){ //new year midnight
            year = year-1;
            day = today.minusHours(1).getDayOfYear();
            buffer.append(year).append("_").append(day);
            return buffer.toString();
        }
        if(hour == 0){ //every midnight
            day = day-1; //save last hour data into previous day
            buffer.append(year).append("_").append(day);
            return buffer.toString();
        }
        buffer.append(year).append("_").append(day);
        return buffer.toString();
    }
    private String labelDayAndYear(String prefix,LocalDateTime today){
        return new StringBuffer().append(prefix).append("_").append(today.getYear()).append("_").append(today.getDayOfYear()).toString();
    }
    private String categoryKey(String category,String classifier){
        return new StringBuffer().append(category).append("_").append(classifier).toString();
    }
    private void initialize(String classifier,MetricsSnapshot metricsSnapshot,LocalDateTime current){
        LocalDateTime _cur = current;//LocalDateTime.now();
        int track = metricsTrackingNumber-1;
        switch (classifier){
            case LeaderBoard.HOURLY:
                LocalDateTime hf = _cur.minusMinutes(_cur.getMinute()).plusHours(1);
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xhf = hf.minusHours(track-i);
                    String xh = xhf.format(DateTimeFormatter.ofPattern("hh:mm a"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xh,0,xhf),_cur);
                }
                break;
            case LeaderBoard.DAILY:
                LocalDateTime df = _cur.plusDays(1);
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xdf = df.minusDays(track-i);
                    String xd = xdf.minusDays(track-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xd,0,xdf),_cur);
                }
                break;
            case LeaderBoard.WEEKLY:
                LocalDateTime wf = _cur.minusDays(_cur.getDayOfWeek().getValue()-1).plusDays(7);//toMonday
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xwf = wf.minusWeeks(track-i);
                    String xw = xwf.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xw,0,xwf),_cur);
                }
                break;
            case LeaderBoard.MONTHLY:
                LocalDateTime mf = _cur.minusDays(_cur.getDayOfMonth()-1).plusMonths(1);//toFirstDayOfMonth
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xmf = mf.minusMonths(track-i);
                    String xm = xmf.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xm,0,xmf),_cur);
                }
                break;
            case LeaderBoard.YEARLY:
                LocalDateTime yf = _cur.minusDays(_cur.getDayOfYear()-1).plusYears(1);//toFirstDayOfYear
                for(int i=0;i<metricsTrackingNumber;i++){
                    LocalDateTime xyf = yf.minusYears(track-i);
                    String xd = xyf.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xd,0,xyf),_cur);
                }
                break;
            default:
                logger.warn("classifier not support");
        }
    }
    private MetricsSnapshot metricsSnapshot(String category,String classifier){
        String ckey = categoryKey(category,classifier);
        return snapshots.computeIfAbsent(ckey,k->{
            MetricsSnapshot pending = new MetricsSnapshot(metricsTrackingNumber,category,classifier);
            pending.bucket(bucket);
            pending.oid(oid);
            initialize(classifier,pending,LocalDateTime.now());
            pending.dataStore(this.dataStore);
            this.dataStore.createIfAbsent(pending,true);
            return pending;
        });
    }
    private MetricsHistory metricsHistory(String category,String classifier,LocalDateTime end,int bufferSize){
        String akey = historyLabel(category,classifier,end);
        return archives.computeIfAbsent(akey,k->{
            MetricsHistory metricsHistory = new MetricsHistory(bufferSize);
            metricsHistory.distributionKey(akey);
            metricsHistory.initializeHourly(end);
            this.dataStore.createIfAbsent(metricsHistory,true);
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

    protected LocalDateTime end(){
        return LocalDateTime.now();
    }
    protected void registerCategory(String category){
        if(pendingUpdates.containsKey(category)) return;
        categories.add(category);
        pendingUpdates.put(category,new StatsDelta(category,0));
    }
}

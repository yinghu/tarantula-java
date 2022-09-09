package com.tarantula.platform.service.metrics;

import com.icodesoftware.*;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
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


    public String name(){
        return name;
    }

    public void setup(ServiceContext serviceContext){
        this.categories = new ArrayList<>();
        this.pendingUpdates = new ConcurrentHashMap<>();
        this.snapshots = new ConcurrentHashMap<>();
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
        String dayAndYear = labelDayAndYear(LocalDateTime.now());
        this.statistics = new SystemStatistics();
        this.statistics.distributionKey(nodeId+Recoverable.PATH_SEPARATOR+dayAndYear);
        this.statistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(statistics,true);
        this.bucket = this.statistics.bucket();
        this.oid = this.statistics.oid();
        logger.warn("Metrics statistics loaded->"+statistics.key().asString());
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


    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.run();
    }

    public void atMidnight(LocalDateTime end){
        SystemStatistics next = new SystemStatistics();
        next.distributionKey(this.serviceContext.nodeId());
        next.label(labelDayAndYear(end));
        next.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(next,true);
        categories.forEach(category->{
            SystemStatisticsEntry entry = (SystemStatisticsEntry)next.entry(category);
            double daily = statistics.entry(category).daily();
            MetricsSnapshot md = metricsSnapshot(category,LeaderBoard.DAILY);
            md.update(daily);
            String xd = end.plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            Property pd = new MetricsProperty(metricsTrackingNumber-1,xd,0);
            md.push(pd);
            this.dataStore.update(md);

            if(end.getDayOfWeek().getValue() != 7){//weekly
                entry.weekly(statistics.entry(category).weekly(),end);
            }
            else if(end.getDayOfWeek().getValue() == 7){
                double weekly = statistics.entry(category).weekly();
                MetricsSnapshot mw = metricsSnapshot(category,LeaderBoard.WEEKLY);
                mw.update(weekly);
                String xw = end.plusDays(7).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                Property pw = new MetricsProperty(metricsTrackingNumber-1,xw,0);
                mw.push(pw);
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
                Property pm = new MetricsProperty(metricsTrackingNumber-1,xm,0);
                mm.push(pm);
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
                Property py = new MetricsProperty(metricsTrackingNumber-1,xy,0);
                my.push(py);
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
                Property property = new MetricsProperty(metricsTrackingNumber-1,xh,0);
                snapshot.push(property);
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
        }
        finally {
            lock.unlock();
        }

    }


    private String labelDayAndYear(LocalDateTime today){
        return today.getYear()+"_"+today.getDayOfYear();
    }
    private String categoryKey(String category,String classifier){
        return new StringBuffer().append(category).append("_").append(classifier).toString();
    }
    private void initialize(String classifier,MetricsSnapshot metricsSnapshot){
        LocalDateTime _cur = LocalDateTime.now();
        int track = metricsTrackingNumber-1;
        switch (classifier){
            case LeaderBoard.HOURLY:
                LocalDateTime hf = _cur.minusMinutes(_cur.getMinute()).plusHours(1);
                for(int i=0;i<metricsTrackingNumber;i++){
                    String xh = hf.minusHours(track-i).format(DateTimeFormatter.ofPattern("hh:mm a"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xh,0));
                }
                break;
            case LeaderBoard.DAILY:
                LocalDateTime df = _cur.plusDays(1);
                for(int i=0;i<metricsTrackingNumber;i++){
                    String xd = df.minusDays(track-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xd,0));
                }
                break;
            case LeaderBoard.WEEKLY:
                LocalDateTime wf = _cur.minusDays(_cur.getDayOfWeek().getValue()-1).plusDays(7);//toMonday
                for(int i=0;i<metricsTrackingNumber;i++){
                    String xw = wf.minusWeeks(track-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xw,0));
                }
                break;
            case LeaderBoard.MONTHLY:
                LocalDateTime mf = _cur.minusDays(_cur.getDayOfMonth()-1).plusMonths(1);//toFirstDayOfMonth
                for(int i=0;i<metricsTrackingNumber;i++){
                    String xm = mf.minusMonths(track-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xm,0));
                }
                break;
            case LeaderBoard.YEARLY:
                LocalDateTime yf = _cur.minusDays(_cur.getDayOfYear()-1).plusYears(1);//toFirstDayOfYear
                for(int i=0;i<metricsTrackingNumber;i++){
                    String xd = yf.minusYears(track-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    metricsSnapshot.initialize(new MetricsProperty(i,xd,0));
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
            initialize(classifier,pending);
            pending.dataStore(this.dataStore);
            this.dataStore.createIfAbsent(pending,true);
            return pending;
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

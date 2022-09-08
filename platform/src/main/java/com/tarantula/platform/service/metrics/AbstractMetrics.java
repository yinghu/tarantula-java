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
        this.statistics.distributionKey(nodeId);
        this.statistics.label(dayAndYear);
        this.statistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(statistics,true);
        this.bucket = this.statistics.bucket();
        this.oid = this.statistics.oid();
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
                if(p.value>0) statistics.entry(p.name).update(p.value).update();
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
        String categoryKey = categoryKey(category,classifier);
        MetricsSnapshot metricsSnapshot = snapshots.computeIfAbsent(categoryKey,k->{
            MetricsSnapshot pending = new MetricsSnapshot(metricsTrackingNumber,category,classifier);
            pending.bucket(bucket);
            pending.oid(oid);
            this.dataStore.createIfAbsent(pending,true);
            return pending;
        });
        logger.warn(metricsSnapshot.name()+"//"+metricsSnapshot.index());
        logger.warn(metricsSnapshot.bucket()+"//"+metricsSnapshot.oid());
        return metricsSnapshot.metrics();
    }

    public Property[] _snapshot(String category, String classifier){
        Property[] properties = new Property[metricsTrackingNumber];
        LocalDateTime _cur = LocalDateTime.now();
        switch (classifier){
            case "hourly":
                LocalDateTime hf = _cur.minusMinutes(_cur.getMinute());
                HourlyMetrics hourlyMetrics = new HourlyMetrics(metricsTrackingNumber);
                hourlyMetrics.name(category);
                hourlyMetrics.bucket(this.bucket);
                hourlyMetrics.oid(this.oid);
                hourlyMetrics.dataStore(this.dataStore);
                if(this.dataStore.load(hourlyMetrics)){
                    logger.warn(hourlyMetrics.key().asString());
                    Property[] mts = hourlyMetrics.metrics();
                    for(int i=0;i<metricsTrackingNumber;i++){
                        properties[i]= mts[i];
                    }
                }else{
                    for(int i=0;i<metricsTrackingNumber;i++){
                        String xh = hf.minusHours(11-i).format(DateTimeFormatter.ofPattern("hh:mm a"));
                        hourlyMetrics.property(new MetricsProperty(i,xh,i));
                    }
                    hourlyMetrics.update();
                }
                break;
            case "daily":
                for(int i=0;i<12;i++){
                    String xd = _cur.minusDays(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(0,xd,1);
                }
                break;
            case "weekly":
                LocalDateTime wf = _cur.minusDays(_cur.getDayOfWeek().getValue()-1);//toMonday
                for(int i=0;i<12;i++){
                    String xw = wf.minusWeeks(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(i,xw,1);
                }
                break;
            case "monthly":
                LocalDateTime mf = _cur.minusDays(_cur.getDayOfMonth()-1);//toFirstDayOfMonth
                for(int i=0;i<12;i++){
                    String xm = mf.minusMonths(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(i,xm,1);
                }
                break;
            case "yearly":
                LocalDateTime yf = _cur.minusDays(_cur.getDayOfYear()-1);//toFirstDayOfYear
                for(int i=0;i<12;i++){
                    String xd = yf.minusYears(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(i,xd,1);
                }
                break;

        }
        return properties;
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
            if(end.getDayOfWeek().getValue() != 7){//weekly
                entry.weekly(statistics.entry(category).weekly(),end);
            }
            if(end.getDayOfMonth() != 1){//monthly
                entry.monthly(statistics.entry(category).monthly(),end);
            }
            if(end.getDayOfYear() != 1 ){//yearly
                entry.yearly(statistics.entry(category).yearly(),end);
                entry.total(statistics.entry(category).total(),end);
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
                //reset hourly metrics
                entry.hourly(0,end);
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
    protected Property[] processMetrics(String category,String classifier){
        Property[] properties = new Property[metricsTrackingNumber];
        LocalDateTime _cur = LocalDateTime.now();
        switch (classifier){
            case "hourly":
                LocalDateTime hf = _cur.minusMinutes(_cur.getMinute());
                HourlyMetrics hourlyMetrics = new HourlyMetrics(metricsTrackingNumber);
                hourlyMetrics.name(category);
                hourlyMetrics.bucket(this.bucket);
                hourlyMetrics.oid(this.oid);
                hourlyMetrics.dataStore(this.dataStore);
                for(int i=0;i<metricsTrackingNumber;i++){
                    String xh = hf.minusHours(11-i).format(DateTimeFormatter.ofPattern("hh:mm a"));
                    hourlyMetrics.property(new MetricsProperty(i,xh,1));
                }
                hourlyMetrics.update();
                break;
            case "daily":
                for(int i=0;i<12;i++){
                    String xd = _cur.minusDays(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(0,xd,1);
                }
                break;
            case "weekly":
                LocalDateTime wf = _cur.minusDays(_cur.getDayOfWeek().getValue()-1);//toMonday
                for(int i=0;i<12;i++){
                    String xw = wf.minusWeeks(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(i,xw,1);
                }
                break;
            case "monthly":
                LocalDateTime mf = _cur.minusDays(_cur.getDayOfMonth()-1);//toFirstDayOfMonth
                for(int i=0;i<12;i++){
                    String xm = mf.minusMonths(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(i,xm,1);
                }
                break;
            case "yearly":
                LocalDateTime yf = _cur.minusDays(_cur.getDayOfYear()-1);//toFirstDayOfYear
                for(int i=0;i<12;i++){
                    String xd = yf.minusYears(11-i).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                    properties[i]= new MetricsProperty(i,xd,1);
                }
                break;

        }
        return properties;
    }
}

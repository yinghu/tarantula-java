package com.tarantula.platform.service.metrics;

import com.icodesoftware.DataStore;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.Statistics;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.statistics.StatsDelta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


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


    private ConcurrentHashMap<String, StatsDelta> pendingUpdates;
    private AtomicBoolean atMidnight;

    protected long pendingUpdateInterval = 10000;
    protected DataStore dataStore;
    private SystemStatistics statistics;
    private ServiceContext serviceContext;
    private ArrayList<String> categories;
    protected TarantulaLogger logger;
    protected String name;

    public String name(){
        return name;
    }

    public void setup(ServiceContext serviceContext){
        this.categories = new ArrayList<>();
        this.pendingUpdates = new ConcurrentHashMap<>();

        //register default categories
        registerCategory(PAYMENT_GOOGLE_STORE_COUNT);
        registerCategory(PAYMENT_APPLE_STORE_COUNT);
        registerCategory(PAYMENT_STRIPE_COUNT);
        registerCategory(PAYMENT_GOOGLE_STORE_AMOUNT);
        registerCategory(PAYMENT_APPLE_STORE_AMOUNT);
        registerCategory(PAYMENT_STRIPE_AMOUNT);

        registerCategory(ACCESS_GOOGLE_LOGIN_COUNT);
        registerCategory(ACCESS_WEB_LOGIN_COUNT);
        registerCategory(ACCESS_DEVICE_LOGIN_COUNT);
        registerCategory(ACCESS_FACEBOOK_LOGIN_COUNT);
        registerCategory(ACCESS_GAME_CENTER_LOGIN_COUNT);
        registerCategory(ACCESS_DEVELOPER_LOGIN_COUNT);
        registerCategory(ACCESS_AMAZON_S3_COUNT);


        registerCategory(GAME_JOIN_COUNT);
        registerCategory(GAME_TIMEOUT_COUNT);

        _setup(serviceContext);
        this.serviceContext = serviceContext;
        atMidnight = new AtomicBoolean(false);
        String nodeId = serviceContext.nodeId();
        String dayAndYear = labelDayAndYear(LocalDateTime.now());
        this.statistics = new SystemStatistics();
        this.statistics.distributionKey(nodeId);
        this.statistics.label(dayAndYear);
        this.statistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(statistics,true);
        serviceContext.schedule(this);
        logger.warn("Metrics ["+name+"] has registered");
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
        if(atMidnight.get()) return;
        _run();
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

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.run();
    }

    public void atMidnight(){
        atMidnight.set(true);
        this._run();
        LocalDateTime end = end();
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
        atMidnight.set(false);
    }

    private String labelDayAndYear(LocalDateTime today){
        return today.getYear()+"_"+today.getDayOfYear();
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

package com.tarantula.test.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.JvmRNG;
import com.icodesoftware.util.TarantulaThreadFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Updated by yinghu lu on 8/31/2019.
 */
public class Main {

    static ExecutorService pool;
    static ScheduledExecutorService scheduler;

    static boolean onFile = false;

    static String[] displayNames = new String[2500];
    static JvmRNG rng = new JvmRNG();
    static int index(){
        return rng.onNext(2500);
    }
    static String accessKey;
    static long httpRequestInterval;
    static int playerUpdateRound = 10;

    static String inventoryKey = "inventory";
    static String campaignKey = "campaign";

    public static void vmain(String[] args) throws Exception{
        HttpCaller httpCaller = new HttpCaller("http://localhost:8090");
        httpCaller._init();
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,
                "570342964778242048-6879508E047E7B7BF8A50EF2951A3A198EE81957-FA80A8B8A29B7C4C4D636032D4BFA571",
                Session.TARANTULA_ACTION,
                "onGameClusterEvent",
                Session.TARANTULA_NAME,
                "1000#ShippingFormCompleted"};
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("playerId", 535221986201178113L);
        System.out.println(httpCaller.post("server",jsonObject.toString().getBytes(),headers));
    }
    public static void main(String[] args) throws Exception{
        JsonObject config = JsonUtil.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("game-presence-settings.json")).get("profile").getAsJsonObject();
        JsonArray adjs = config.get("adjectives").getAsJsonArray();
        JsonArray nouns = config.get("nouns").getAsJsonArray();
        int[] i={0};
        adjs.forEach(adj->{
            String pre = adj.getAsString();
            nouns.forEach(noun->{
                displayNames[i[0]++]=pre+noun.getAsString();
            });
        });
        Properties properties = new Properties();
        try(InputStream inputStream = new FileInputStream("load.properties")){
            properties.load(inputStream);
            onFile = true;
        }catch (Exception ex){
            try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("load.properties")){
                properties.load(inputStream);
            }catch (Exception exx){
                throw new RuntimeException("No load properties found");
            }
        }
        String host = properties.getProperty("host");
        String game = properties.getProperty("game");
        int batch = Integer.parseInt(properties.getProperty("batch"));
        int poolSize = Integer.parseInt(properties.getProperty("pool.size"));
        boolean scheduledPlay = Boolean.parseBoolean(properties.getProperty("scheduled.play"));
        httpRequestInterval = Long.parseLong(properties.getProperty("http.request.interval.ms"));
        accessKey = properties.getProperty("access.key");
        boolean usePlayerPrefix = Boolean.parseBoolean(properties.getProperty("use.player.prefix"));
        String playerPrefix = properties.getProperty("player.prefix");
        boolean udpTested = Boolean.parseBoolean(properties.getProperty("test.udp"));
        int udpReceiveTimeout = Integer.parseInt(properties.getProperty("udp.receive.timeout"));
        long udpPlayInterval = Long.parseLong(properties.getProperty("udp.play.interval.ms"));
        int udpTestRounds = Integer.parseInt(properties.getProperty("udp.test.rounds"));
        LoadResult.playerPrefix = usePlayerPrefix?playerPrefix:"random";
        LoadResult.host = host;
        LoadResult.poolSize = poolSize;
        LoadResult.batch = batch;
        LoadResult.startTime = LocalDateTime.now();
        LoadResult.udpTested = udpTested;
        LoadResult.udpReceiveTimeout = udpReceiveTimeout;
        LoadResult.udpTestRounds = udpTestRounds;
        if(scheduledPlay) {
            runSimulationOnSchedule(game,host, usePlayerPrefix ? playerPrefix : null, batch, poolSize, udpTested, udpReceiveTimeout, udpTestRounds, httpRequestInterval,udpPlayInterval);
        }
        else{
            runSimulation(game,host, usePlayerPrefix ? playerPrefix : null, batch, poolSize, udpTested, udpReceiveTimeout, udpTestRounds, httpRequestInterval);
        }
    }

    private static void runSimulation(String game,String host,String playerPrefix,int batch,int poolSize,boolean udpTested,int timeout,int duration,long requestWaiting) throws Exception{
        HttpCaller httpCaller = new HttpCaller(host);
        httpCaller._init();
        pool = Executors.newFixedThreadPool(poolSize,new TarantulaThreadFactory("test-load"));
        int ix = 0;
        for(int i = 0;i<batch;i++){
            CountDownLatch waiting = new CountDownLatch(poolSize);
            for(int x=0;x<poolSize;x++){
                String uname = playerPrefix!=null?(playerPrefix+"-"+ix):UUID.randomUUID().toString();
                ix++;
                Player simulator = new Player(httpCaller,waiting,game,uname,udpTested,timeout,duration);
                pool.execute(simulator);
                Thread.sleep(requestWaiting);
            }
            waiting.await();
        }
        pool.shutdown();
        LoadResult.print(onFile);
    }

    private static void runSimulationOnSchedule(String game,String host,String playerPrefix,int batch,int poolSize,boolean udpTested,int timeout,int udpRounds,long requestWaiting,long udpPlayInterval) throws Exception{
        HttpCaller httpCaller = new HttpCaller(host);
        httpCaller._init();
        scheduler = new ScheduledThreadPoolExecutor(poolSize,new TarantulaThreadFactory("test-load"));
        CountDownLatch waiting = new CountDownLatch(batch);
        for(int i = 0;i<batch;i++){
            String uname = playerPrefix!=null?(playerPrefix+"-"+i):UUID.randomUUID().toString();
            ScheduledPlayer simulator = new ScheduledPlayer(httpCaller,waiting,game,uname,i+1,udpTested,timeout,udpRounds);
            scheduler.schedule(()->{
                simulator.join();
                if(simulator.joined){
                    scheduler.schedule(()->simulator.play(scheduler,udpPlayInterval),udpPlayInterval,TimeUnit.MILLISECONDS);
                }
            },requestWaiting,TimeUnit.MILLISECONDS);
        }
        waiting.await();
        scheduler.shutdown();
        LoadResult.print(onFile);
    }
}

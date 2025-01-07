package com.icodesoftware.etcd;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.HttpClientProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.Base64Util;
import com.icodesoftware.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.locks.ReentrantLock;

import static com.icodesoftware.util.HttpCaller.*;

public class EtcdManager {

    private static final String PUT = "v3/kv/put";
    private static final String GET = "v3/kv/range";
    private static final String WATCH = "v3/watch";
    private static final String DEL = "v3/kv/deleterange";
    private static final String TXN = "v3/kv/txn";
    private static final int REQUEST_TIMEOUT_SECONDS = TIME_OUT;

    private static final int MAX_EVENT_SIZE = 100;
    private static final TarantulaLogger logger = JDKLogger.getLogger(EtcdManager.class);

    //EtcdManager.put("hello1","teset100");
    //EtcdManager.get("hello1");
    //EtcdManager.watch("hello","\0");//>= hello
    //EtcdManager.watch("hello","hellp");//prefix
    //EtcdManager.watch("\0","\0");

    private static Thread watcher;
    private static Thread starter;
    private static ConcurrentHashMap<String,ETCDWatchListener> watcherIndex = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, EtcdNode> nodeIndexByName = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Integer, EtcdPartition> nodeIndexByPartition = new ConcurrentHashMap<>();
    private static EtcdNode localNode;
    private static int partitionNumber;
    private static int clusterSize;
    private static int pingCount;
    private static String watchStart;
    private static String watchEnd;
    private final static ArrayList<EtcdNode> pending = new ArrayList<>();
    private static ReentrantLock lock = new ReentrantLock(true);
    private final static CountDownLatch joined = new CountDownLatch(1);
    private static boolean running;
    private EtcdManager(){}

    public static EtcdNode localNode(){
        return localNode;
    }
    public static boolean await(){
        try{
            joined.await();
        }catch (Exception ex){

        }
        return running;
    }
    public static void registerETCDWatchListener(ETCDWatchListener listener){
        watcherIndex.put(listener.watchKey(),listener);
    }
    public static void setup(ServiceContext serviceContext) throws Exception{
        ClusterProvider.Node node = serviceContext.node();
        EtcdNode etcdNode = EtcdNode.create(node.nodeName(),node.address());
        etcdNode.httpClientProvider = serviceContext.httpClientProvider();
        etcdNode.etcdHost = node.etcdHost();
        start(etcdNode,node.bucketNumber(),node.clusterSize(),node.pingCount(),node.clusterNameSuffix());
    }
    public static void start(EtcdNode localNode,int partitionNumber,int clusterSize,int pingCount,String clusterName) throws Exception{
        EtcdManager.registerETCDWatchListener(new NodeJoinListener());
        EtcdManager.registerETCDWatchListener(new NodePingListener());
        EtcdManager.registerETCDWatchListener(new NodeJoinedListener());
        EtcdManager.registerETCDWatchListener(new NodeClaimListener());
        EtcdManager.localNode = localNode;
        EtcdManager.partitionNumber = partitionNumber;
        EtcdManager.clusterSize = clusterSize;
        EtcdManager.pingCount = pingCount;
        EtcdManager.watchStart = clusterName+"#";
        WatchKey.PREFIX = EtcdManager.watchStart;//overriding default
        EtcdManager.watchEnd = clusterName+"$";
        for(int i=0;i<partitionNumber;i++){
            nodeIndexByPartition.put(i,new EtcdPartition(i));
        }
        watcher = new Thread(()->{
            EtcdManager.watch(watchStart,watchEnd);
        },"tarantula-homing-agent-watcher");
        watcher.start();
        EtcdManager.register(WatchEvent.join(localNode.name()));
        starter = new Thread(()->{
            for(int i=0;i<10;i++){
                try{
                    logger.warn("Waiting for join process ...");
                    Thread.sleep(500);
                }catch (Exception ex){
                    //ignore
                }
            }
            running = EtcdManager.register();
            joined.countDown();
            int loop = 0;
            while(running){
                try{
                    Thread.sleep(1*1000);
                    EtcdManager.register(WatchEvent.ping(localNode.name()));
                    loop++;
                    if(loop>=2){
                        EtcdManager.check();
                        loop = 0;
                    }
                }catch (Exception ex){
                    //ignore
                    logger.warn("Error : ",ex);
                    loop=0;
                }
            }
        },"tarantula-homing-agent-join");
        starter.start();
    }

    public static void shutdown() throws Exception{
        running = false;
        watcher.interrupt();
    }

    public static void register(EtcdEvent event){
        String key = event.key().asString();
        Recoverable.DataBuffer buffer = BufferProxy.buffer(MAX_EVENT_SIZE,false);
        event.write(buffer);
        buffer.flip();
        String value = Base64Util.toBase64String(buffer.src());
        put(key,value);
    }

    public static <T extends EtcdEvent> void create(T event){
        String key = event.key().asString();
        Recoverable.DataBuffer buffer = BufferProxy.buffer(MAX_EVENT_SIZE,false);
        event.write(buffer);
        buffer.flip();
        String value = Base64Util.toBase64String(buffer.src());
        put(key,value);
    }

    public static <T extends EtcdEvent> void update(T event){
        String key = event.key().asString();
        Recoverable.DataBuffer buffer = BufferProxy.buffer(MAX_EVENT_SIZE,false);
        event.write(buffer);
        buffer.flip();
        String value = Base64Util.toBase64String(buffer.src());
        txn(key,value,event.revision());
    }

    public static <T extends EtcdEvent> void load(T event){
        String key = event.key().asString();
        get(key,(k,v,r)->{
            Recoverable.DataBuffer buffer = BufferProxy.wrap(v);
            event.read(buffer);
            event.revision(r);
        });
    }
    public static <T extends EtcdEvent> void delete(T event){
        String key = event.key().asString();
        delete(key);
    }
    //event callbacks
    public static boolean register(){
        boolean available;
        try {
            lock.lock();
            available = nodeIndexByName.size() < clusterSize;
        }finally {
            lock.unlock();
        }
        if(!available) return false;
        nodeIndexByName.put(localNode.name(),localNode);
        EtcdManager.register(ClaimEvent.create(localNode.name(),localNode.httpEndpoint));
        return true;
    }
    public static void joined(EtcdEvent event){
        if(event.nodeName.equals(localNode.name())) return;
        EtcdManager.register(JoinedEvent.create(localNode.name(),localNode.httpEndpoint));
    }
    public static void claim(EtcdNode node){
        try{
            lock.lock();
            nodeIndexByName.put(node.name(),node);
            partition();
        }finally {
            lock.unlock();
        }
        logger.warn("Node ["+node.name()+"] has claimed");
    }
    public static void ping(EtcdNode node){
        if(node.name().equals(localNode.name())) return;
        EtcdNode pending = nodeIndexByName.get(node.name());
        if(pending==null) return;
        pending.nextPing.decrementAndGet();
    }

    public static List<EtcdNode> view(){
        List<EtcdNode> view = new ArrayList<>();
        nodeIndexByName.forEach((k,v)->view.add(v));
        Collections.sort(view,new NodeComparator());
        return view;
    }

    public static List<Integer> partition(String nodeName){
        List<Integer> view = new ArrayList<>();
        nodeIndexByPartition.forEach((k,v)->{
            if(v.onPartition().name().equals(nodeName)) view.add(k);
        });
        return view;
    }
    public static EtcdPartition partition(byte[] key){
        int p = Math.abs(Arrays.hashCode(key))%partitionNumber;
        for(;;){
            try{
                EtcdPartition pending =  nodeIndexByPartition.get(p);
                if(pending != null) return pending;
                Thread.sleep(10);
            }catch (Exception ex){
                //ignore;
                logger.warn("Error on partition : "+p,ex);
            }
        }
    }
    public static void check(){
        try{
            lock.lock();
            pending.clear();
            nodeIndexByName.forEach((k,check)->{
                if(!check.name().equals(localNode.name()) && check.nextPing.getAndSet(pingCount) == pingCount){
                    pending.add(check);
                }
            });
            if(pending.size()>0){
                pending.forEach(kickoff->nodeIndexByName.remove(kickoff.name()));
                partition();
            }
        }finally {
            lock.unlock();
        }
    }
    private static void partition(){
        pending.clear();
        nodeIndexByName.forEach((k,n)->pending.add(n));
        Collections.sort(pending,new NodeComparator());
        int sz = pending.size();
        for(int i=0;i<partitionNumber;i++){
            int p =  i % sz;
            EtcdNode node = pending.get(p);
            nodeIndexByPartition.get(i).onPartition(node);
            logger.warn("partition ["+i+"] on  node ["+node.name()+"/"+node.httpEndpoint+"]");
        }
    }

    //'{"compare":[{"target":"CREATE","key":"Zm9v","createRevision":"2"}],"success":[{"requestPut":{"key":"Zm9v","value":"YmFy"}}]}'
    private static boolean txn(String key,String value,long revision){
        try {
            HttpClientProvider httpCaller = localNode.httpClientProvider;
            JsonObject target = new JsonObject();
            target.addProperty("target","CREATE");
            target.addProperty("key",key);
            target.addProperty("createRevision",revision);
            JsonArray compare = new JsonArray();
            compare.add(target);
            JsonObject transaction = new JsonObject();
            JsonObject kv = new JsonObject();
            kv.addProperty("key",key);
            kv.addProperty("value",value);
            transaction.add("requestPut",kv);
            JsonArray success = new JsonArray();
            success.add(transaction);
            JsonObject request = new JsonObject();
            request.add("compare",compare);
            request.add("success",success);
            ResponseData data = new ResponseData();
            int code = httpCaller.request(client->{
                HttpResponse<String> response = client.send(build(TXN,request.toString()),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                data.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) throw new RuntimeException(data.dataAsString);
            JsonObject resp = JsonUtil.parse(data.dataAsString);
            return resp.get("succeeded").getAsBoolean();
        }catch (Exception ex){
            logger.error("Error on etcd txn",ex);
            return false;
        }
    }
    private static boolean put(String key,String value){
        try {
            HttpClientProvider httpCaller = localNode.httpClientProvider;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key",key);
            jsonObject.addProperty("value",value);
            ResponseData data = new ResponseData();
            int code = httpCaller.request(client->{
                HttpResponse<String> response = client.send(build(PUT,jsonObject.toString()),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                data.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) throw new RuntimeException(data.dataAsString);
            return true;
        }catch (Exception ex){
            logger.error("Error on etcd put",ex);
            return false;
        }
    }

    private static void get(String key, EtcdEventParser.OnKeyValue keyValue){
        try {
            HttpClientProvider httpCaller = localNode.httpClientProvider;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key",key);
            ResponseData data = new ResponseData();
            int code = httpCaller.request(client->{
                HttpResponse<String> response = client.send(build(GET,jsonObject.toString()),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                data.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) throw new RuntimeException(data.dataAsString);
            EtcdEventParser.parse(data.dataAsString,keyValue);
        }catch (Exception ex){
            logger.error("Error on etcd get",ex);
        }
    }

    private static boolean delete(String key){
        try {
            HttpClientProvider httpCaller = localNode.httpClientProvider;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("key",key);
            ResponseData data = new ResponseData();
            int code = httpCaller.request(client->{
                HttpResponse<String> response = client.send(build(DEL,jsonObject.toString()),HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                data.dataAsString = response.body();
                return response.statusCode();
            });
            if(code!=200) throw new RuntimeException(data.dataAsString);
            JsonObject resp = JsonUtil.parse(data.dataAsString);
            return resp.get("deleted").getAsInt()>0;
        }catch (Exception ex){
            logger.error("Error on etcd delete",ex);
            return false;
        }
    }

    private static void watch(String key,String end){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", Base64Util.toBase64String(key.getBytes()).toString());
        jsonObject.addProperty("range_end",Base64Util.toBase64String(end.getBytes()).toString());
        JsonObject data = new JsonObject();
        data.add("create_request",jsonObject);
        try {
            HttpClientProvider httpCaller = localNode.httpClientProvider;
            httpCaller.request((client)->{
                Watcher sub = new Watcher();
                sub.completableFuture = client.sendAsync(build(WATCH,data.toString()), HttpResponse.BodyHandlers.fromLineSubscriber(sub));
                sub.completableFuture.join();
                return 0;
            });
        }catch (Exception ex){
            logger.error("Error on etcd watch",ex);
        }
    }

    private static HttpRequest build(String path,String json){
        return HttpRequest.newBuilder()
                .uri(URI.create(localNode.etcdHost+path)).version(HttpClient.Version.HTTP_2).timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header(ACCEPT,ACCEPT_JSON).header(CONTENT_TYPE,ACCEPT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    //etcd watch streaming callback
    private static class Watcher implements Flow.Subscriber<String>,ETCDWatchListener {
        public CompletableFuture<?> completableFuture;
        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(String item) {
            EtcdEventParser.onWatch(item, event->{
                ETCDWatchListener listener = watcherIndex.getOrDefault(event.key,this);
                listener.onWatched(event);
            });
            this.subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }

        @Override
        public String watchKey() {
            return "";
        }

        @Override
        public void onWatched(EtcdEvent event) {
            logger.warn(event.toString()+" has no listener registered");
        }
    }
}

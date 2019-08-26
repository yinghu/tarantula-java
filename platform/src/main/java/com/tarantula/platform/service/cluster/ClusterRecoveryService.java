package com.tarantula.platform.service.cluster;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import com.tarantula.Distributable;
import com.tarantula.Event;
import com.tarantula.Serviceable;
import com.tarantula.TarantulaLogger;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.MapStoreVotingEvent;
import com.tarantula.platform.service.persistence.berkeley.RecoveryBatch;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ClusterRecoveryService implements Serviceable {
    private static TarantulaLogger logger = JDKLogger.getLogger(ClusterRecoveryService.class);
    private final ClientConfig dConfig;
    private final ClientConfig iConfig;
    private final String recoverPath;
    private final ScheduledExecutorService scheduledExecutorService;
    private final AtomicBoolean dLoaded;
    private final AtomicBoolean iLoaded;
    private final int retries;
    public ClusterRecoveryService(ClientConfig dConfig, ClientConfig iConfig, String rPath, ScheduledExecutorService scheduledExecutorService, AtomicBoolean dRecovered,AtomicBoolean iRecovered,int retries){
        this.dConfig = dConfig;
        this.iConfig = iConfig;
        this.recoverPath = rPath;
        this.scheduledExecutorService = scheduledExecutorService;
        this.dLoaded = dRecovered;
        this.iLoaded = iRecovered;
        this.retries = retries;
    }
    private void _writeDisk(String fn,RecoveryBatch rb,ConcurrentHashMap<String,RecoveryBatch> cache,String dataPath){
        try{
            Path tp = Paths.get(dataPath);
            if(!Files.exists(tp)){
                Files.createDirectories(tp);
            }
            FileChannel fc = new FileOutputStream(tp.toFile().getPath()+ FileSystems.getDefault().getSeparator()+fn).getChannel();
            for(Event e : rb.pending){
                if(e!=null){
                    fc.write(ByteBuffer.wrap(e.payload()));
                }
            }
            fc.close();
            cache.remove(fn);
            logger.warn("Wrote disk->"+tp.toFile().getPath()+"/"+fn);
        }catch (Exception ex){
            logger.error("Failed on disk writing",ex);
        }
    }
    private void _recoverFromCluster(ClientConfig clientConfig,String dPath,AtomicBoolean loaded) throws Exception{
        AtomicInteger tc = new AtomicInteger(0);
        ConcurrentHashMap<String, RecoveryBatch> mlist = new ConcurrentHashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for(int t=0;t<retries;t++){
            try{
                HazelcastInstance hc = HazelcastClient.newHazelcastClient(clientConfig);
                Iterator<Member> im = hc.getCluster().getMembers().iterator();
                if(im.hasNext()){
                    Member fm = im.next();
                    String rid = UUID.randomUUID().toString();
                    ITopic<Event> rpt = hc.getTopic(rid);
                    rpt.addMessageListener((m)->{
                        Event e = m.getMessageObject();
                        if(e.retries()>0){
                            RecoveryBatch es = mlist.computeIfAbsent(e.source(),(k)-> new RecoveryBatch());
                            es.pending[e.version()]=e;
                            boolean reset = e.version()==99||e.payload().length<100000;
                            if(reset){
                                es.checked.set(reset);
                            }
                            if(es.checked.get()){
                                boolean full = true;
                                for(int i=0;i<e.version();i++){
                                    if(es.pending[i]==null){
                                        full = false;
                                    }
                                }
                                if(full){
                                    tc.incrementAndGet();
                                    this.scheduledExecutorService.execute(new OneTimeRunner(100,()->{
                                        _writeDisk(e.source(),es,mlist,recoverPath+FileSystems.getDefault().getSeparator()+dPath);
                                    }));
                                }
                            }
                        }
                        if(e.retries()==0){
                            countDownLatch.countDown();
                        }
                        if(e.retries()==tc.get()){
                            countDownLatch.countDown();
                        }
                    });
                    hc.getTopic(fm.getUuid()).publish(new MapStoreVotingEvent(fm.getUuid(),rid,fm.getUuid(),Distributable.DATA_SCOPE));
                    countDownLatch.await();
                    loaded.set(true);
                    Thread.sleep(5000);
                }
                hc.shutdown();
                break;
            }
            catch (Exception ex){
                logger.warn("retry->"+ex.getMessage());
                Thread.sleep(3000);
            }
        }
    }

    @Override
    public void start() throws Exception {
        logger.warn("Cleaning existing files and starting to recover from cluster ...");
        String tmp = recoverPath+FileSystems.getDefault().getSeparator()+"data";
        Path path = Paths.get(tmp);
        if(Files.exists(path)){
            for(String s : path.toFile().list()){
                Files.delete(Paths.get(tmp+FileSystems.getDefault().getSeparator()+s));
            }
        }
        tmp = recoverPath+FileSystems.getDefault().getSeparator()+"integration";
        Path ipath = Paths.get(tmp);
        if(Files.exists(ipath)){
            for(String s : ipath.toFile().list()){
                Files.delete(Paths.get(tmp+FileSystems.getDefault().getSeparator()+s));
            }
        }
        _recoverFromCluster(this.dConfig,"data",dLoaded);
        _recoverFromCluster(this.iConfig,"integration",iLoaded);
    }

    @Override
    public void shutdown() throws Exception {

    }
}

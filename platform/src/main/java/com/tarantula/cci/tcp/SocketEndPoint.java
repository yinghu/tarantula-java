package com.tarantula.cci.tcp;

import com.google.gson.GsonBuilder;
import com.tarantula.Session;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.cci.RequestHandler;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.bootstrap.TarantulaExecutorServiceFactory;
import com.tarantula.platform.service.EndPoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;

/**
 * updated by yinghu lu on 5/7/2020.
 */
public class SocketEndPoint implements EndPoint{

    private static final JDKLogger log = JDKLogger.getLogger(SocketEndPoint.class);

    private String address;
    private int port;
    private int backLog;

    private Thread ioThread;
    private ExecutorService dispatcherPool;
    private int workSize = 8;
    private String inboundThreadPoolSetting;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private boolean running;


    private Resource resource;
    private ConcurrentLinkedDeque<PendingRequest> pQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentHashMap<String,PendingRequest> pMap = new ConcurrentHashMap<>();
    private GsonBuilder builder;

    @Override
    public void start() throws Exception {
        this.running = true;
        TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool,psize,rh)->{
            this.dispatcherPool = pool;
            this.workSize = psize;
        });
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(PendingData.class,new PendingDataDeserializer());
        InetSocketAddress ip = this.address==null?new InetSocketAddress(this.port):new InetSocketAddress(this.address,this.port);
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.bind(ip);
        this.selector = Selector.open();
        this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        for(int i=0;i<workSize;i++){
            int sp = i;
            this.dispatcherPool.execute(()->{
                while (running){
                    try{
                        PendingRequest pendingRequest = pQueue.poll();
                        if(pendingRequest!=null){
                            List<String> plist = pendingRequest.readBuffer();
                            for(String js : plist){
                                PendingData pendingData = builder.create().fromJson(js,PendingData.class);
                                if(pendingData.serverId!=null){
                                    pendingRequest.serverId(pendingData.serverId);
                                }
                                //log.warn(">>>>>>"+pendingData.path);
                                RequestHandler requestHandler = resource.requestHandler("/"+pendingData.path.split("/")[1]);
                                SocketSession socketSession = new SocketSession(pendingRequest,pendingData);
                                requestHandler.onRequest(socketSession);
                            }
                        }
                        else{
                            Thread.sleep(sp*3+1);
                        }
                    }catch (Exception ex){
                        //ignore
                        ex.printStackTrace();
                    }
                }
            });
        }
        ioThread = new Thread(()->{
            while (running){
                try{
                    //update pending first
                    int rc = selector.select();
                    if(rc==0){
                        continue;
                    }
                    Set<SelectionKey>  rkey = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = rkey.iterator();
                    while (iterator.hasNext()){
                        SelectionKey k = iterator.next();
                        iterator.remove();
                        try{
                            if(!k.isValid()){
                                continue;
                            }
                            if(k.isAcceptable()){
                                accept();
                            }
                            else if(k.isReadable()){
                                read(k);
                            }
                            else if(k.isWritable()){
                                write(k);
                            }
                        }catch (IOException ioException){
                            //close channel and cancel key on io exception
                            PendingRequest pr = pMap.get(k.attachment());
                            StringBuffer sp = new StringBuffer("{serverId:");
                            sp.append("\"").append(pr.serverId()).append("\"}");
                            PendingData pendingData = new PendingData("/push/streaming","index/user","onDisconnect",sp.toString().getBytes());
                            pendingData.headers.put(Session.TARANTULA_SERVER_ID,pr.serverId());
                            SocketSession send = new SocketSession(pr,pendingData);
                            resource.requestHandler("/push").onRequest(send);
                            log.warn("Connection from ["+pr.serverId()+"] closed with ["+ioException.getMessage()+"]");
                            k.channel().close();
                            k.cancel();
                        }
                    }
                }catch (Exception ex){
                    //ignore if errors
                }
            }
            log.info("IO socket Selector stopped");
        },"Tarantula-socket-io-listener");
        ioThread.start();
        log.info("Socket endpoint started ["+port+"] with back log ["+backLog+"]");
    }
    private void accept() throws IOException{
        SocketChannel psc = this.serverSocketChannel.accept();
        psc.configureBlocking(false);
        SelectionKey selectionKey = psc.register(selector,SelectionKey.OP_READ,UUID.randomUUID().toString());
        PendingRequest pendingRequest = new PendingRequest(selectionKey);
        pMap.putIfAbsent((String) selectionKey.attachment(),pendingRequest);
        pQueue.offer(pendingRequest);
    }
    private void read(SelectionKey key) throws IOException{
        SocketChannel sc = (SocketChannel) key.channel();
        PendingRequest pendingRequest = pMap.get(key.attachment());
        pendingRequest.readIO(sc);
        pQueue.offer(pendingRequest);
    }
    private void write(SelectionKey key) throws IOException{
        SocketChannel sc = (SocketChannel) key.channel();
        PendingRequest pendingRequest = pMap.get(key.attachment());
        pendingRequest.writeIO(sc);
    }
    @Override
    public void shutdown() throws Exception {
        this.running = false;
        dispatcherPool.shutdown();
        serverSocketChannel.close();
        selector.close();
        log.info("Socket endpoint shut down");
    }

    @Override
    public String name() {
        return "Socket-endpoint";
    }


    @Override
    public void address(String address) {
        this.address = address;
    }

    @Override
    public void backlog(int backlog) {
        this.backLog = backlog;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public void secured(boolean secured) {

    }

    @Override
    public void password(String password) {

    }

    @Override
    public void inboundThreadPoolSetting(String inboundThreadPoolSetting) {
        this.inboundThreadPoolSetting = inboundThreadPoolSetting;
    }
    public void resource(Resource resource){
        this.resource = resource;
    }
    public void setup(ServiceContext serviceContext){
    }

    public void waitForData(){

    }
}

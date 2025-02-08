package com.icodesoftware.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.icodesoftware.util.VirtualThreadExecutor;
import com.sun.net.httpserver.HttpServer;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

/*
tarantula.endpoint.http.address=10.0.0.2 ( optional )
tarantula.endpoint.http.port=8090
tarantula.endpoint.http.backlog=100
tarantula.endpoint.http.pool.in.setting=http-inbound,8,32,8,60,100
*/

public class HttpEndpointService implements EndPoint {

    protected String address;
    protected int port;
    protected int backlog;

    protected String inboundThreadPoolSetting;
    protected String configuration;
    protected Executor executorService;
    private Serviceable retryPool;

    protected HttpServer server;

    protected boolean started;

    protected Resource resource;

    protected ServiceContext serviceContext;

    protected MetricsListener metricsListener;

    public HttpEndpointService(){
        metricsListener =(k,v)->{};
    }
    @Override
    public void address(String address) {
        this.address = address;
    }

    @Override
    public void port(int port) {
        this.port = port;
    }

    @Override
    public int port(){
        return port;
    }
    @Override
    public void backlog(int backlog) {
        this.backlog = backlog;
    }
    @Override
    public void inboundThreadPoolSetting(String inboundThreadPoolSetting) {
        this.inboundThreadPoolSetting = inboundThreadPoolSetting;
    }

    @Override
    public String name() {
        return EndPoint.HTTP_ENDPOINT;
    }


    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }
    @Override
    public void start() throws Exception {
        if(onResource()) return;
        bootstrap();
        onStart();
        server.start();
        started = true;
    }

    @Override
    public void shutdown() throws Exception {
        if(!started) return;
        onStop();
        if(retryPool!=null) retryPool.shutdown();
        this.server.stop(0);
    }

    public void resource(Resource resource){
        this.resource = resource;
    }

    public void resource(String resource){
        this.configuration = resource;
    }
    @Override
    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener == null) return;
        this.metricsListener = metricsListener;
    }
    private void bootstrap() throws Exception{
        TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
            this.executorService = pool;
            this.retryPool = rh;
        });
        InetSocketAddress ip = this.address==null? new InetSocketAddress(this.port) : new InetSocketAddress(this.address,this.port);
        if(address==null) address = ip.getHostName();
        server = HttpServer.create(ip,this.backlog);
        server.setExecutor(this.executorService);
    }
    protected boolean onResource() throws Exception{
        if(configuration == null) return false;
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configuration)){
            JsonObject settings = JsonUtil.parse(inputStream);
            this.address = settings.has("address")? settings.get("address").getAsString() : null;
            this.port = settings.has("port")? settings.get("port").getAsInt() : 8090;
            this.backlog = settings.has("backlog")? settings.get("backlog").getAsInt() : 100;
            int inboundPermits = settings.has("inboundConcurrentPermits")?settings.get("inboundConcurrentPermits").getAsInt() : 8;
            this.executorService = VirtualThreadExecutor.create(inboundPermits);
            InetSocketAddress ip = this.address==null? new InetSocketAddress(this.port) : new InetSocketAddress(this.address,this.port);
            if(address==null) address = ip.getHostName();
            server = HttpServer.create(ip,this.backlog);
            server.setExecutor(this.executorService);
            JsonArray endpoints = settings.get("endpoints").getAsJsonArray();
            endpoints.forEach(e->{
                JsonObject endpoint = e.getAsJsonObject();
                String path = endpoint.get("path").getAsString();
                try{
                    AbstractHttpHandler httpHandler = (AbstractHttpHandler) Class.forName(endpoint.get("handler").getAsString()).getConstructor().newInstance();
                    httpHandler.setup(serviceContext);
                    server.createContext(path,httpHandler);
                }catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            });
            server.start();
            started = true;
        }
        return true;
    }

    protected void onStart() throws Exception{}
    protected void onStop() throws Exception{}
}

package com.icodesoftware.protocol;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
/*
tarantula.endpoint.http.address=10.0.0.2 ( optional )
tarantula.endpoint.http.port=8090
tarantula.endpoint.http.backlog=100
tarantula.endpoint.http.pool.in.setting=http-inbound,8,32,8,60,100
*/

abstract public class HttpEndpointService implements EndPoint {

    protected String address;
    private int port;
    private String inboundThreadPoolSetting;
    private int backlog;

    private ExecutorService tpool;
    private Serviceable retryPool;
    protected HttpServer hserver;

    protected boolean started;
    protected Resource resource;

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

    @Override
    public void start() throws Exception {
        TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
            this.tpool = pool;
            this.retryPool = rh;
        });
        InetSocketAddress ip = this.address==null?new InetSocketAddress(this.port):new InetSocketAddress(this.address,this.port);
        if(address==null) address = ip.getHostName();
        hserver = HttpServer.create(ip,this.backlog);
        hserver.setExecutor(this.tpool);
        onStart();
        hserver.start();
        started = true;
    }

    @Override
    public void shutdown() throws Exception {
        if(!started) return;
        onStop();
        retryPool.shutdown();
        tpool.shutdown();
        this.hserver.stop(0);
    }

    public void resource(Resource resource){
        this.resource = resource;
    }
    @Override
    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener == null) return;
        this.metricsListener = metricsListener;
    }

    abstract protected void onStart() throws Exception;
    abstract protected void onStop() throws Exception;
}

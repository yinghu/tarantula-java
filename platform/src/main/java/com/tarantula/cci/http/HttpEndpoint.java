package com.tarantula.cci.http;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

import com.sun.net.httpserver.HttpServer;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.Serviceable;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.bootstrap.TarantulaExecutorServiceFactory;
import com.tarantula.platform.service.EndPoint;


public class HttpEndpoint implements EndPoint {

	private static final JDKLogger log = JDKLogger.getLogger(HttpEndpoint.class);
	
	private String address;
	private int port;
	private String inboundThreadPoolSetting;
	private int backlog;


	private ExecutorService tpool;
	private Serviceable retryPool;
	private HttpServer hserver;

	private boolean started;

    private Resource resource;
	public void start() throws Exception {
		TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool,poolSize,rh)->{
			this.tpool = pool;
			this.retryPool = rh;
		});
		InetSocketAddress ip = this.address==null?new InetSocketAddress(this.port):new InetSocketAddress(this.address,this.port);
		hserver = HttpServer.create(ip,this.backlog);
		hserver.setExecutor(this.tpool);
		log.info("Initializing web context ...");
		HttpRootHandler root = new HttpRootHandler(this.resource.requestHandler("/"));
		this.hserver.createContext("/",root);

		HttpResourceHandler resource = new HttpResourceHandler(this.resource.requestHandler("/resource"));
		this.hserver.createContext("/resource",resource);

		HttpHealthCheckHandler healthCheckHandler = new HttpHealthCheckHandler();
		this.hserver.createContext("/health",healthCheckHandler);

		HttpContentHandler httpContentHandler = new HttpContentHandler(this.resource.requestHandler("/content"));
		this.hserver.createContext("/content",httpContentHandler);

		HttpUserHandler httpUserHandler = new HttpUserHandler(this.resource.requestHandler("/user"));
		this.hserver.createContext("/user", httpUserHandler);

		HttpServiceHandler httpServiceHandler = new HttpServiceHandler(this.resource.requestHandler("/service"));
		this.hserver.createContext("/service",httpServiceHandler);

		HttpApplicationHandler httpApplicationHandler = new HttpApplicationHandler(this.resource.requestHandler("/application"));
		this.hserver.createContext("/application", httpApplicationHandler);
        hserver.start();
        started = true;
        log.info("Tarantula HTTP Endpoint is listening on ["+ip.toString()+"]");
	}
	public void shutdown() throws Exception {
		if(started){
			retryPool.shutdown();
			tpool.shutdown();
        	this.hserver.stop(0);
        	log.info("Tarantula HTTP Endpoint Closed");
		}
		else{
			log.warn("Tarantula HTTP Endpoint not running locally");
		}
	}


	public void address(String address) {
		this.address = address;
	}


	public void port(int port) {
		this.port = port;
	}
	public void backlog(int backlog) {
		this.backlog = backlog;
	}

	public void secured(boolean secured){

	}
	public void password(String password){

	}

	public void inboundThreadPoolSetting(String inboundThreadPoolSetting){
		this.inboundThreadPoolSetting = inboundThreadPoolSetting;
	}
	public String name(){
		return "HTTP-endpoint";
	}


	public void resource(Resource resource){
		this.resource = resource;
	}

	public void setup(ServiceContext serviceContext){
	}
	public void waitForData(){

	}
}

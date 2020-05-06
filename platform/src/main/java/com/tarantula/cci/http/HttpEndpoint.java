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
		HttpRootHandler root = new HttpRootHandler();
		root.resource(this.resource);
		this.hserver.createContext(root.path(),root);

		HttpUploadHandler upload = new HttpUploadHandler();
		upload.resource(this.resource);
		this.hserver.createContext(upload.path(),upload);

		HttpResourceHandler httpResourceHandler = new HttpResourceHandler();
		httpResourceHandler.resource(this.resource);
		this.hserver.createContext(httpResourceHandler.path(),httpResourceHandler);

		HttpHealthCheckHandler healthCheckHandler = new HttpHealthCheckHandler();
		healthCheckHandler.resource(this.resource);
		this.hserver.createContext(healthCheckHandler.path(),healthCheckHandler);

		HttpUserHandler httpUserHandler = new HttpUserHandler();
		httpUserHandler.resource(this.resource);
		this.hserver.createContext(httpUserHandler.path(), httpUserHandler);

		HttpServiceHandler httpServiceHandler = new HttpServiceHandler();
		httpServiceHandler.resource(this.resource);
		this.hserver.createContext(httpServiceHandler.path(),httpServiceHandler);

		HttpApplicationHandler httpApplicationHandler = new HttpApplicationHandler();
		httpApplicationHandler.resource(this.resource);
		this.hserver.createContext(httpApplicationHandler.path(), httpApplicationHandler);

		HttpDedicatedServerHandler httpDedicatedHandler = new HttpDedicatedServerHandler();
		httpDedicatedHandler.resource(this.resource);
		this.hserver.createContext(httpDedicatedHandler.path(), httpDedicatedHandler);

		HttpAdminHandler httpAdminHandler = new HttpAdminHandler();
		httpAdminHandler.resource(this.resource);
		this.hserver.createContext(httpAdminHandler.path(),httpAdminHandler);

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

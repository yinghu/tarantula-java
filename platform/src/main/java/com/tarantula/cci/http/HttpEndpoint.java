package com.tarantula.cci.http;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.sun.net.httpserver.HttpServer;
import com.icodesoftware.logging.JDKLogger;
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
		TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
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

		HttpPushServerHandler httpPushServerHandler = new HttpPushServerHandler();
		httpPushServerHandler.resource(this.resource);
		this.hserver.createContext(httpPushServerHandler.path(),httpPushServerHandler);

		HttpAdminHandler httpAdminHandler = new HttpAdminHandler();
		httpAdminHandler.resource(this.resource);
		this.hserver.createContext(httpAdminHandler.path(),httpAdminHandler);

		HttpPresenceHandler httpPresenceHandler = new HttpPresenceHandler();
		httpPresenceHandler.resource(this.resource);
		this.hserver.createContext(httpPresenceHandler.path(),httpPresenceHandler);

		HttpAccountHandler httpAccountHandler = new HttpAccountHandler();
		httpAccountHandler.resource(this.resource);
		this.hserver.createContext(httpAccountHandler.path(),httpAccountHandler);

		HttpSudoHandler httpSudoHandler = new HttpSudoHandler();
		httpSudoHandler.resource(this.resource);
		this.hserver.createContext(httpSudoHandler.path(),httpSudoHandler);

		HttpViewHandler httpViewHandler = new HttpViewHandler();
		httpViewHandler.resource(this.resource);
		this.hserver.createContext(httpViewHandler.path(),httpViewHandler);

		HttpGameServerHandler httpGameServerHandler = new HttpGameServerHandler();
		httpGameServerHandler.resource(this.resource);
		this.hserver.createContext(httpGameServerHandler.path(),httpGameServerHandler);

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

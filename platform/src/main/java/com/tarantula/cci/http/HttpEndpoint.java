package com.tarantula.cci.http;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;
import com.sun.net.httpserver.HttpServer;
import com.icodesoftware.logging.JDKLogger;

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

    private MetricsListener metricsListener;

    public HttpEndpoint(){
		metricsListener =(k,v)->{};
	}

	public void start() throws Exception {
		TarantulaExecutorServiceFactory.createExecutorService(this.inboundThreadPoolSetting,(pool, poolSize, rh)->{
			this.tpool = pool;
			this.retryPool = rh;
		});
		InetSocketAddress ip = this.address==null?new InetSocketAddress(this.port):new InetSocketAddress(this.address,this.port);
		hserver = HttpServer.create(ip,this.backlog);
		hserver.setExecutor(this.tpool);
		HttpRootHandler root = new HttpRootHandler(metricsListener);
		root.resource(this.resource);
		this.hserver.createContext(root.path(),root);

		HttpUploadHandler upload = new HttpUploadHandler(metricsListener);
		upload.resource(this.resource);
		this.hserver.createContext(upload.path(),upload);

		HttpResourceHandler httpResourceHandler = new HttpResourceHandler(metricsListener);
		httpResourceHandler.resource(this.resource);
		this.hserver.createContext(httpResourceHandler.path(),httpResourceHandler);

		HttpHealthCheckHandler healthCheckHandler = new HttpHealthCheckHandler(metricsListener);
		healthCheckHandler.resource(this.resource);
		this.hserver.createContext(healthCheckHandler.path(),healthCheckHandler);

		HttpUserHandler httpUserHandler = new HttpUserHandler(metricsListener);
		httpUserHandler.resource(this.resource);
		this.hserver.createContext(httpUserHandler.path(), httpUserHandler);

		HttpServiceHandler httpServiceHandler = new HttpServiceHandler(metricsListener);
		httpServiceHandler.resource(this.resource);
		this.hserver.createContext(httpServiceHandler.path(),httpServiceHandler);

		HttpAdminHandler httpAdminHandler = new HttpAdminHandler(metricsListener);
		httpAdminHandler.resource(this.resource);
		this.hserver.createContext(httpAdminHandler.path(),httpAdminHandler);

		HttpPresenceHandler httpPresenceHandler = new HttpPresenceHandler(metricsListener);
		httpPresenceHandler.resource(this.resource);
		this.hserver.createContext(httpPresenceHandler.path(),httpPresenceHandler);

		HttpAccountHandler httpAccountHandler = new HttpAccountHandler(metricsListener);
		httpAccountHandler.resource(this.resource);
		this.hserver.createContext(httpAccountHandler.path(),httpAccountHandler);

		HttpSudoHandler httpSudoHandler = new HttpSudoHandler(metricsListener);
		httpSudoHandler.resource(this.resource);
		this.hserver.createContext(httpSudoHandler.path(),httpSudoHandler);

		HttpViewHandler httpViewHandler = new HttpViewHandler(metricsListener);
		httpViewHandler.resource(this.resource);
		this.hserver.createContext(httpViewHandler.path(),httpViewHandler);

		HttpGameServerHandler httpGameServerHandler = new HttpGameServerHandler(metricsListener);
		httpGameServerHandler.resource(this.resource);
		this.hserver.createContext(httpGameServerHandler.path(),httpGameServerHandler);


		HttpDevelopmentHandler httpDevelopmentHandler = new HttpDevelopmentHandler(metricsListener);
		httpDevelopmentHandler.resource(this.resource);
		this.hserver.createContext(httpDevelopmentHandler.path(),httpDevelopmentHandler);

		hserver.start();
        started = true;
        log.info("Tarantula HTTP Endpoint is listening on ["+ip+"]");
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

	public void inboundThreadPoolSetting(String inboundThreadPoolSetting){
		this.inboundThreadPoolSetting = inboundThreadPoolSetting;
	}

	public String name(){
		return EndPoint.HTTP_ENDPOINT;
	}

	public void resource(Resource resource){
		this.resource = resource;
	}
	@Override
	public void registerMetricsListener(MetricsListener metricsListener){
		if(metricsListener == null) return;
		this.metricsListener = metricsListener;
	}

}

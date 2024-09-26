package com.tarantula.cci.http;

import com.icodesoftware.protocol.HttpEndpointService;
import com.icodesoftware.logging.JDKLogger;

public class HttpEndpoint extends HttpEndpointService{

	private static final JDKLogger log = JDKLogger.getLogger(HttpEndpoint.class);


    public HttpEndpoint(){
		super();
	}

	public void onStart() throws Exception {

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

        log.info("Tarantula HTTP Endpoint is listening on ["+address+"]");
	}

	@Override
	protected void onStop() throws Exception {
		log.warn("Tarantula HTTP Endpoint is stopping on ["+address+"]");
	}
}

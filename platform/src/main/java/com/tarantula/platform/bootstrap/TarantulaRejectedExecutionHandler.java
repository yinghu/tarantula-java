package com.tarantula.platform.bootstrap;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import com.tarantula.platform.service.Serviceable;
import com.tarantula.logging.JDKLogger;


public class TarantulaRejectedExecutionHandler implements RejectedExecutionHandler, Serviceable {
	
	private static final JDKLogger log = JDKLogger.getLogger(TarantulaRejectedExecutionHandler.class);
	
	private final String name;
    private final int maxPoolSize;

	public TarantulaRejectedExecutionHandler(final String name,final int overflow ){
		this.name = name;
        this.maxPoolSize = overflow;
	}
	
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor tpool) {
		log.warn("Task discharged on thread pool ["+this.name+"/"+maxPoolSize+"] with total tasks ["+tpool.getTaskCount()+"] on threads ["+tpool.getActiveCount()+"]");
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void shutdown() throws Exception {

	}
}

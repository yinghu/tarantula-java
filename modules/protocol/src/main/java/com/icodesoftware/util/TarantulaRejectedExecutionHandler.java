package com.icodesoftware.util;

import com.icodesoftware.service.Serviceable;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;


public class TarantulaRejectedExecutionHandler implements RejectedExecutionHandler, Serviceable {
	

	private final String name;
    private final int maxPoolSize;

	public TarantulaRejectedExecutionHandler(final String name, final int overflow ){
		this.name = name;
        this.maxPoolSize = overflow;
	}
	
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor tpool) {
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void shutdown() throws Exception {

	}
}

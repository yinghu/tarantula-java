package com.tarantula.platform.bootstrap;

import com.icodesoftware.service.Serviceable;

import java.util.concurrent.CountDownLatch;

public class ServiceBootstrap extends Thread{
	private final CountDownLatch waiting;
	private final CountDownLatch next;
	private final Serviceable service;
	private final boolean required;

	public ServiceBootstrap(final CountDownLatch waiting,final CountDownLatch next,final Serviceable service,final String name,final boolean required){
		super("Bootstrap-"+name);
		this.setPriority(Thread.MAX_PRIORITY);
		this.waiting = waiting;
		this.next = next;
		this.service = service;
		this.required = required;
	}
    @Override
	public void run() {
		try{
			this.waiting.await();
			this.service.start();
			if(this.next!=null){
				this.next.countDown();
			}
		}catch(Exception ex){
			if(required){
				ex.printStackTrace();
				System.exit(-1);
			}
			if(this.next!=null){
				this.next.countDown();
			}
		}		
	}		
}

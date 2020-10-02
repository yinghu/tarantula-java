package com.icodesoftware.util;

import com.icodesoftware.service.Serviceable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class TarantulaRetryExecutionHandler implements RejectedExecutionHandler, Serviceable {


	private final String name;
    private final int maxPoolSize;
    private final ConcurrentLinkedQueue<Runnable> oQueue;
	private final AtomicBoolean started;
	private int totalRetries;

	private ExecutorService executorService;
	public TarantulaRetryExecutionHandler(final String name, final int overflow ){
		this.name = name;
        this.maxPoolSize = overflow;
        this.oQueue = new ConcurrentLinkedQueue<>();
        this.started = new AtomicBoolean(false);
	}
	
	public void rejectedExecution(Runnable runnable, ThreadPoolExecutor tpool) {
		//log.warn("Task retrying on thread pool ["+this.name+"/"+maxPoolSize+"] with total tasks ["+tpool.getTaskCount()+"] on threads ["+tpool.getActiveCount()+"]");
		oQueue.offer(runnable);
		this.totalRetries++;
		try{this.start();}catch (Exception ex){}
	}

	@Override
	public void start() throws Exception {
		if(started.compareAndSet(false,true)){
			executorService = Executors.newFixedThreadPool(maxPoolSize,new TarantulaThreadFactory(name+"-backup"));
			for(int i=0;i<maxPoolSize;i++){
				executorService.execute(()->{
					while (true){
						try{
							Runnable task = oQueue.poll();
							if(task!=null){
								task.run();
							}
							else{
								Thread.sleep(100);
							}
						}catch (Exception ex){
							//ignore
						}
					}
				});
			}
		}
	}

	@Override
	public void shutdown() throws Exception {
		if(started.get()){
			executorService.shutdown();
		}
	}
}

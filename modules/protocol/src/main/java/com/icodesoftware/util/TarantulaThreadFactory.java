package com.icodesoftware.util;


import java.util.concurrent.ThreadFactory;

public class TarantulaThreadFactory implements ThreadFactory{

	private static String prefix ="tarantula-";
	
	private int tnumber =1;
	
	private String tname;
	
		
	public TarantulaThreadFactory(String suffix){
		this.tname = prefix+suffix;
		
	}
	private Thread _newThread(Runnable runnable){
		Thread t = new Thread(runnable);
		t.setName(this.tname+"-"+(this.tnumber++));
		return t;
	}
	public synchronized Thread newThread(Runnable r) {
		return this._newThread(r);
	}

}

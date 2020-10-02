package com.tarantula.platform.bootstrap;

import java.util.LinkedList;


import com.icodesoftware.service.Serviceable;
import com.icodesoftware.logging.TarantulaLogManager;

public class ShutdownHook extends Thread {


	LinkedList<Serviceable> _services = new LinkedList<>();
	
	@Override
	public void run(){
		
		try{
			for(Serviceable s:_services){
				try{s.shutdown();}catch (Exception ex){ex.printStackTrace();}
			}
			Thread.sleep(3000);
		}catch(Exception ex){
            ex.printStackTrace();
            System.exit(-1);
		}
		TarantulaLogManager.shutdown();//close log manager
	}
	public void register(Serviceable service){
		this._services.addLast(service);
	}
}

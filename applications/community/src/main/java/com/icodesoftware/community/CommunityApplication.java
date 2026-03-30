package com.icodesoftware.community;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {
		PostOffice.start();
		PostOffice.subscribe("message");
		PostOffice.subscribe("message");
		PostOffice.subscribe("message");
		for(int x=0;x<10;x++){
			new Thread(()->{
				for (int i=0;i<1000;i++)
				{
					try {
						PostOffice.message("message>"+i);
						Thread.sleep(10);
					} catch (InterruptedException e) {}
				}
			}).start();
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {PostOffice.stop();}));
		SpringApplication.run(CommunityApplication.class, args);
	}

	@PreDestroy
	public static void stop(){
		PostOffice.stop();
	}

}

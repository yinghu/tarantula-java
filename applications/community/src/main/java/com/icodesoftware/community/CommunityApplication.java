package com.icodesoftware.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {
		PostOffice.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {PostOffice.stop();}));
		SpringApplication.run(CommunityApplication.class, args);
	}

}

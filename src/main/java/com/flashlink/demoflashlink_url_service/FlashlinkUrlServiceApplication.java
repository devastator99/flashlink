package com.flashlink.demoflashlink_url_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FlashlinkUrlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashlinkUrlServiceApplication.class, args);
	}

}

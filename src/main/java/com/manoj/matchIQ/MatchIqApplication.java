package com.manoj.matchIQ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MatchIqApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatchIqApplication.class, args);
	}

}

package com.changeowl.githubingestionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GithubIngestionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubIngestionServiceApplication.class, args);
	}

}

package com.app.reliable_job_queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ReliableJobQueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReliableJobQueueApplication.class, args);
	}

}

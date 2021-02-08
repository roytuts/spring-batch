package com.roytuts.spring.batch.multiple.parallel.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class SpringBatchMultipleJobsApp {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchMultipleJobsApp.class, args);
	}

}

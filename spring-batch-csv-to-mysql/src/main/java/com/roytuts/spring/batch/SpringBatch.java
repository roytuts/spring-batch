package com.roytuts.spring.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.roytuts.spring.batch")
public class SpringBatch {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatch.class, args);
	}

}

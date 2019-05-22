package com.jeejava.spring.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.jeejava.spring.batch")
public class SpringBatch {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatch.class, args);
	}

}

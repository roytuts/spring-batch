package com.roytuts.spring.batch.task.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class SpringBatchTaskScheduler {

	@Autowired
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Scheduled(cron = "*/10 * * * * *")
	public void run() {
		try {
			JobExecution execution = jobLauncher.run(job,
					new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
			System.out.println("Job Status : " + execution.getStatus());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("Done");
	}

}

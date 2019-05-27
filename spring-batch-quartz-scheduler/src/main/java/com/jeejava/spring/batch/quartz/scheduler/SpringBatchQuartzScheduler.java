package com.jeejava.spring.batch.quartz.scheduler;

import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SpringBatchQuartzScheduler extends QuartzJobBean {

	private JobLocator jobLocator;

	private JobLauncher jobLauncher;

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		Map<String, Object> jobMap = context.getMergedJobDataMap();
		String jobName = (String) jobMap.get("jobName");
		try {
			JobExecution execution = jobLauncher.run(jobLocator.getJob(jobName),
					new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
			System.out.println("Job Status : " + execution.getStatus());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Done");
	}

}

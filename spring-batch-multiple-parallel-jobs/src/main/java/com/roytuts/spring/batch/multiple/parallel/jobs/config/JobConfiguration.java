package com.roytuts.spring.batch.multiple.parallel.jobs.config;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.roytuts.spring.batch.multiple.parallel.jobs.listener.JobListener;
import com.roytuts.spring.batch.multiple.parallel.jobs.listener.ReadListener;
import com.roytuts.spring.batch.multiple.parallel.jobs.listener.StepListener;
import com.roytuts.spring.batch.multiple.parallel.jobs.listener.WriteListener;
import com.roytuts.spring.batch.multiple.parallel.jobs.model.Item;
import com.roytuts.spring.batch.multiple.parallel.jobs.processor.JobProcessor;
import com.roytuts.spring.batch.multiple.parallel.jobs.reader.JobReader;
import com.roytuts.spring.batch.multiple.parallel.jobs.writer.JobWriter;

@Configuration
@EnableScheduling
@EnableBatchProcessing
public class JobConfiguration {

	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public ItemReader<Item> jobReader() {
		return new JobReader(new Item("1000", LocalDate.now()));
	}

	@Bean
	public ItemProcessor<Item, Item> jobProcessor() {
		return new JobProcessor();
	}

	@Bean
	public ItemWriter<Item> jobWriter() {
		return new JobWriter();
	}

	@Bean
	public ItemReadListener<Item> readListener() {
		return new ReadListener<Item>();
	}

	@Bean
	public StepExecutionListener stepListener() {
		return new StepListener();
	}

	@Bean
	public ItemWriteListener<Item> writeListener() {
		return new WriteListener<Item>();
	}

	@Bean
	public JobExecutionListener jobListener() {
		return new JobListener();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("Step1").listener(stepListener()).listener(readListener())
				.listener(writeListener()).tasklet((contribution, chunkContext) -> {
					System.out.println("Tasklet has run");
					return RepeatStatus.FINISHED;
				}).build();
	}

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("Step2").listener(stepListener()).listener(readListener())
				.listener(writeListener()).<String, String>chunk(3)
				.reader(new ListItemReader<>(Arrays.asList("1", "2", "3", "4", "5", "6")))
				.processor(new ItemProcessor<String, String>() {
					@Override
					public String process(String item) throws Exception {
						return String.valueOf(Integer.parseInt(item) * -1);
					}
				}).writer(items -> {
					for (String item : items) {
						System.out.println(">> " + item);
					}
				}).build();
	}

	@Bean
	public Job job1() {
		return this.jobBuilderFactory.get("Job1").listener(jobListener()).incrementer(new RunIdIncrementer())
				.start(step1()).next(step2()).build();
	}

	@Bean
	public Step anotherStep() {
		return this.stepBuilderFactory.get("AnotherStep").listener(stepListener()).listener(readListener())
				.listener(writeListener()).tasklet((contribution, chunkContext) -> {
					System.out.println("Yet another Tasklet!");
					return RepeatStatus.FINISHED;
				}).build();
	}

	@Bean
	public Job job2() {
		return this.jobBuilderFactory.get("Job2").listener(jobListener()).incrementer(new RunIdIncrementer())
				.start(anotherStep()).build();
	}

	@Bean
	public Job job3() {
		Step step = stepBuilderFactory.get("Job3Step").listener(readListener()).listener(stepListener())
				.listener(writeListener()).<Item, Item>chunk(1).reader(jobReader()).processor(jobProcessor())
				.writer(jobWriter()).build();

		return jobBuilderFactory.get("Job3").listener(jobListener()).start(step).build();
	}

	@Bean
	public ResourcelessTransactionManager transactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(15);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(30);
		return taskExecutor;
	}

	@Bean
	public JobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setTaskExecutor(taskExecutor()); // Or below line
		// jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	@Bean
	public JobOperator jobOperator(JobRegistry jobRegistry) throws Exception {
		SimpleJobOperator jobOperator = new SimpleJobOperator();
		jobOperator.setJobExplorer(jobExplorer);
		jobOperator.setJobLauncher(jobLauncher());
		jobOperator.setJobRegistry(jobRegistry);
		jobOperator.setJobRepository(jobRepository);
		return jobOperator;
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
		return jobRegistryBeanPostProcessor;
	}

	// @Scheduled(cron = "*/5 * * * * *")
	@Scheduled(fixedRate = 10000)
	public void run1() {
		Map<String, JobParameter> confMap = new HashMap<>();
		confMap.put("time", new JobParameter(System.currentTimeMillis()));
		JobParameters jobParameters = new JobParameters(confMap);
		try {
			jobLauncher().run(job1(), jobParameters);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}

	}

	// @Scheduled(cron = "*/5 * * * * *")
	@Scheduled(fixedRate = 10000)
	public void run2() {
		Map<String, JobParameter> confMap = new HashMap<>();
		confMap.put("time", new JobParameter(System.currentTimeMillis()));
		JobParameters jobParameters = new JobParameters(confMap);
		try {
			jobLauncher().run(job2(), jobParameters);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}

	// @Scheduled(cron = "*/5 * * * * *")
	@Scheduled(fixedRate = 10000)
	public void run3() {
		Map<String, JobParameter> confMap = new HashMap<>();
		confMap.put("time", new JobParameter(System.currentTimeMillis()));
		JobParameters jobParameters = new JobParameters(confMap);
		try {
			jobLauncher().run(job3(), jobParameters);
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
}

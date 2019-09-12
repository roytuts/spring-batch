package com.roytuts.spring.batch.config;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.support.DatabaseType;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.roytuts.spring.batch.itemprocessor.PersonItemProcessor;
import com.roytuts.spring.batch.quartz.scheduler.SpringBatchQuartzScheduler;
import com.roytuts.spring.batch.vo.Person;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

	@Bean
	@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
	public Person person() {
		return new Person();
	}

	@Bean
	@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
	public ItemProcessor<Person, Person> itemProcessor() {
		return new PersonItemProcessor();
	}
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/roytuts");
		dataSource.setUsername("root");
		dataSource.setPassword("");

		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource("org/springframework/batch/core/schema-drop-mysql.sql"));
		databasePopulator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));

		DatabasePopulatorUtils.execute(databasePopulator, dataSource);
		return dataSource;
	}

	@Bean
	public ResourcelessTransactionManager txManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean
	public JobRepository jbRepository(DataSource dataSource, ResourcelessTransactionManager transactionManager)
			throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDatabaseType(DatabaseType.MYSQL.getProductName());
		factory.setDataSource(dataSource);
		factory.setTransactionManager(transactionManager);
		return factory.getObject();
	}

	@Bean
	public JobLauncher jbLauncher(JobRepository jbRepository) {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jbRepository);
		return jobLauncher;
	}

	@Bean
	public BeanWrapperFieldSetMapper<Person> beanWrapperFieldSetMapper() {
		BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setPrototypeBeanName("person");
		return fieldSetMapper;
	}

	@Bean
	public FlatFileItemReader<Person> fileItemReader(BeanWrapperFieldSetMapper<Person> beanWrapperFieldSetMapper) {
		FlatFileItemReader<Person> fileItemReader = new FlatFileItemReader<>();
		fileItemReader.setResource(new ClassPathResource("person.csv"));

		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setNames("id", "firstName", "lastName");

		DefaultLineMapper<Person> defaultLineMapper = new DefaultLineMapper<>();
		defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
		defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

		fileItemReader.setLineMapper(defaultLineMapper);

		return fileItemReader;
	}
	
	@Bean(destroyMethod = "")
	public StaxEventItemWriter<Person> staxEventItemWriter(Jaxb2Marshaller marshaller) {
		StaxEventItemWriter<Person> staxEventItemWriter = new StaxEventItemWriter<>();
		staxEventItemWriter.setResource(new FileSystemResource("C:/workspace/person" + LocalDate.now() + ".xml"));
		staxEventItemWriter.setMarshaller(marshaller);
		staxEventItemWriter.setRootTagName("personInfo");

		return staxEventItemWriter;
	}

	@Bean
	public Job jobCsvXml(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("jobCsvXml").incrementer(new RunIdIncrementer()).flow(step).end().build();
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ResourcelessTransactionManager transactionManager,
			ItemReader<Person> reader, ItemWriter<Person> writer, ItemProcessor<Person, Person> processor) {
		return stepBuilderFactory.get("step1").transactionManager(transactionManager).<Person, Person>chunk(2)
				.reader(reader).processor(processor).writer(writer).build();
	}
	
	
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setClassesToBeBound(Person.class);

		return jaxb2Marshaller;
	}
	
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor=new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
		
		return jobRegistryBeanPostProcessor;
	}
	
	@Bean
	public JobDetailFactoryBean jobDetailFactoryBean(Job jobCsvXml, JobLauncher jobLauncher, JobRegistry jobLocator) {
		JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();

		jobDetailFactoryBean.setJobClass(SpringBatchQuartzScheduler.class);

		Map<String, Object> map = new HashMap<>();
		map.put("jobName", jobCsvXml.getName());
		map.put("jobLauncher", jobLauncher);
		map.put("jobLocator", jobLocator);

		jobDetailFactoryBean.setJobDataAsMap(map);

		return jobDetailFactoryBean;
	}
	
	@Bean
	public CronTriggerFactoryBean cronTriggerFactoryBean(JobDetailFactoryBean jobDetail) {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setJobDetail(jobDetail.getObject());
		cronTriggerFactoryBean.setStartDelay(2000);
		cronTriggerFactoryBean.setCronExpression("*/2 * * * * ?");
		
		return cronTriggerFactoryBean;
	}
	
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean cronTrigger) {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setTriggers(cronTrigger.getObject());

		return schedulerFactoryBean;
	}

}

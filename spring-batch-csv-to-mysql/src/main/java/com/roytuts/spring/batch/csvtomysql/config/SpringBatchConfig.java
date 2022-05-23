package com.roytuts.spring.batch.csvtomysql.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.support.DatabaseType;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.roytuts.spring.batch.csvtomysql.itemprocessor.PersonItemProcessor;
import com.roytuts.spring.batch.csvtomysql.vo.Person;

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
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/roytuts");
		dataSource.setUsername("root");
		dataSource.setPassword("root");

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
	public JobLauncher jbLauncher(JobRepository jobRepository) {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
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

	@Bean
	public JdbcBatchItemWriter<Person> jdbcBatchItemWriter(DataSource dataSource,
			BeanPropertyItemSqlParameterSourceProvider<Person> sqlParameterSourceProvider) {
		JdbcBatchItemWriter<Person> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
		jdbcBatchItemWriter.setDataSource(dataSource);
		jdbcBatchItemWriter.setItemSqlParameterSourceProvider(sqlParameterSourceProvider);
		jdbcBatchItemWriter.setSql("insert into person(id,firstName,lastName) values (:id, :firstName, :lastName)");

		return jdbcBatchItemWriter;
	}

	@Bean
	public BeanPropertyItemSqlParameterSourceProvider<Person> beanPropertyItemSqlParameterSourceProvider() {
		return new BeanPropertyItemSqlParameterSourceProvider<>();
	}

	@Bean
	public Job jobCsvMysql(JobBuilderFactory jobBuilderFactory, Step step) {
		return jobBuilderFactory.get("jobCsvMysql").incrementer(new RunIdIncrementer()).flow(step).end().build();
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ResourcelessTransactionManager transactionManager,
			ItemReader<Person> reader, ItemWriter<Person> writer, ItemProcessor<Person, Person> processor) {
		return stepBuilderFactory.get("step1").transactionManager(transactionManager).<Person, Person>chunk(2)
				.reader(reader).processor(processor).writer(writer).build();
	}

}

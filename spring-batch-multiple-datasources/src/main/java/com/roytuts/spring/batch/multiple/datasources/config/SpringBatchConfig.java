package com.roytuts.spring.batch.multiple.datasources.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.roytuts.spring.batch.multiple.datasources.fieldset.mapper.UserFieldSetMapper;
import com.roytuts.spring.batch.multiple.datasources.itemprocessor.UserItemProcessor;
import com.roytuts.spring.batch.multiple.datasources.statementsetter.PersonsPreparedStatementSetter;
import com.roytuts.spring.batch.multiple.datasources.vo.User;

@Configuration
public class SpringBatchConfig {

	@Autowired
	@Qualifier("h2DataSource")
	private DataSource dataSource;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	private static final String QUERY_INSERT_PERSONS = "INSERT " + "INTO persons(name, email) " + "VALUES (?, ?)";

	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(platformTransactionManager);
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	// creates an item reader
	public ItemReader<User> reader() {
		FlatFileItemReader<User> reader = new FlatFileItemReader<User>();
		// look for file user.csv
		reader.setResource(new ClassPathResource("person.csv"));
		// line mapper
		DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();
		// each line with comma separated
		lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
		// map file's field with object
		lineMapper.setFieldSetMapper(new UserFieldSetMapper());
		reader.setLineMapper(lineMapper);
		return reader;
	}

	@Bean
	// creates an instance of our UserItemProcessor for transformation
	public ItemProcessor<User, User> processor() {
		return new UserItemProcessor();
	}

	@Bean
	@Transactional(rollbackFor = Exception.class)
	// creates item writer
	public ItemWriter<User> writer() {

		JdbcBatchItemWriter<User> batchItemWriter = new JdbcBatchItemWriter<>();
		batchItemWriter.setJdbcTemplate(namedParameterJdbcTemplate);
		batchItemWriter.setSql(QUERY_INSERT_PERSONS);

		ItemPreparedStatementSetter<User> valueSetter = new PersonsPreparedStatementSetter();

		batchItemWriter.setItemPreparedStatementSetter(valueSetter);

		return batchItemWriter;
	}

	@Bean
	public Job importUserJob(Step step) throws Exception {
		// need incrementer to maintain execution state
		return new JobBuilder("importUserJob", jobRepository()).incrementer(new RunIdIncrementer()).flow(step).end()
				.build();
	}

	@Bean
	public Step step1(ItemReader<User> reader, ItemWriter<User> writer, ItemProcessor<User, User> processor)
			throws Exception {
		// chunk uses how much data to write at a time
		// In this case, it writes up to five records at a time.
		// Next, we configure the reader, processor, and writer
		return new StepBuilder("step1", jobRepository()).<User, User>chunk(5, platformTransactionManager).reader(reader)
				.processor(processor).writer(writer).build();
	}

}

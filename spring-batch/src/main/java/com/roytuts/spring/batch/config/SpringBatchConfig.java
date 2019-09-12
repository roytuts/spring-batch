package com.roytuts.spring.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.roytuts.spring.batch.fieldset.mapper.UserFieldSetMapper;
import com.roytuts.spring.batch.itemprocessor.UserItemProcessor;
import com.roytuts.spring.batch.vo.User;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

	@Bean
	// creates an item reader
	public ItemReader<User> reader() {
		FlatFileItemReader<User> reader = new FlatFileItemReader<User>();
		// look for file user.csv
		reader.setResource(new ClassPathResource("user.csv"));
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
	// creates item writer
	public ItemWriter<User> writer() {
		FlatFileItemWriter<User> writer = new FlatFileItemWriter<User>();
		// output file path
		writer.setResource(new FileSystemResource("C:/workspace/transformed_user.csv"));
		// delete if the file already exists
		writer.setShouldDeleteIfExists(true);
		// create lines for writing to file
		DelimitedLineAggregator<User> lineAggregator = new DelimitedLineAggregator<User>();
		// delimit field by comma
		lineAggregator.setDelimiter(",");
		// extract field from ItemReader
		BeanWrapperFieldExtractor<User> fieldExtractor = new BeanWrapperFieldExtractor<User>();
		// use User object's properties
		fieldExtractor.setNames(new String[] { "name", "email" });
		lineAggregator.setFieldExtractor(fieldExtractor);
		// write whole data
		writer.setLineAggregator(lineAggregator);
		return writer;
	}

	@Bean
	// define job which is built from step
	public Job importUserJob(JobBuilderFactory jobs, Step step) {
		// need incrementer to maintain execution state
		return jobs.get("importUserJob").incrementer(new RunIdIncrementer()).flow(step).end().build();
	}

	@Bean
	// define step
	public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<User> reader, ItemWriter<User> writer,
			ItemProcessor<User, User> processor) {
		// chunk uses how much data to write at a time
		// In this case, it writes up to five records at a time.
		// Next, we configure the reader, processor, and writer
		return stepBuilderFactory.get("step1").<User, User>chunk(5).reader(reader).processor(processor).writer(writer)
				.build();
	}

}

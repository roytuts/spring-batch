package com.roytuts.spring.batch.multiple.datasources.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DataSourceConfig {

	@Autowired
	private Environment environment;

	@Primary
	@Bean(name = "h2DataSource")
	public DataSource h2DataSource() {
		EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();
		return embeddedDatabaseBuilder.addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
				.addScript("classpath:org/springframework/batch/core/schema-h2.sql").setType(EmbeddedDatabaseType.H2)
				.build();
	}

	@Bean(name = "mySQLDataSource")
	public DataSource mySQLDataSource() {
		return DataSourceBuilder.create().driverClassName(environment.getProperty("spring.datasource.driverClassName"))
				.url(environment.getProperty("spring.datasource.url"))
				.username(environment.getProperty("spring.datasource.username"))
				.password(environment.getProperty("spring.datasource.password")).build();
	}

	@Bean
	public PlatformTransactionManager mySQLDataSourceTransactionManager() {
		return new DataSourceTransactionManager(mySQLDataSource());
	}

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
		return new NamedParameterJdbcTemplate(mySQLDataSource());
	}

	@Primary
	@Bean(name = "platformTransactionManager")
	public PlatformTransactionManager platformTransactionManager() {
		return new DataSourceTransactionManager(h2DataSource());
	}

}

package org.atlas.engine.financialexchange.participants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinancialExchangeParticipantsApplication {

	public static void main(String[] args) {
		String databaseUrl = System.getProperty("database.url", "jdbc:postgresql://127.0.0.1/atlas-database");
		String databaseUsernmae = System.getProperty("database.username", "postgres");
		String databasePassword = System.getProperty("database.password", "password1");
		
		System.setProperty("spring.datasource.url", databaseUrl);
		System.setProperty("spring.datasource.username", databaseUsernmae);
		System.setProperty("spring.datasource.password", databasePassword);
		System.setProperty("spring.jpa.generate", "true");
		System.setProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation", "true");
		System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQL9Dialect");

		SpringApplication.run(FinancialExchangeParticipantsApplication.class, args);
	}

}

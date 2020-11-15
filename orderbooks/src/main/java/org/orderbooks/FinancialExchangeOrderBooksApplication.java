package org.atlas.engine.financialexchange.orderbooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinancialExchangeOrderBooksApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialExchangeOrderBooksApplication.class, args);
	}

}


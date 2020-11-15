package org.atlas.engine.financialexchange.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinancialExchangeProductsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialExchangeProductsApplication.class, args);
	}

}

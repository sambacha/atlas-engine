package org.atlas.engine.financialexchange.orders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FinancialExchangeOrdersApplication {

	private static final Log logger = LogFactory.getLog(FinancialExchangeOrdersApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FinancialExchangeOrdersApplication.class, args);
		logger.debug("Orders service started");
	}

}


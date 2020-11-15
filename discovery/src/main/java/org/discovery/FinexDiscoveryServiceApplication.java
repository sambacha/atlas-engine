package org.atlas.engine.financialexcange.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class AtlasDiscoveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtlasDiscoveryServiceApplication.class, args);
	}

}

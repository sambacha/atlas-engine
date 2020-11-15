package org.atlas.engine.financialexchange.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class AtlasConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtlasConfigServerApplication.class, args);
	}

}

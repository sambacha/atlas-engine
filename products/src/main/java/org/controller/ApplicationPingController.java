package org.atlas.engine.financialexchange.products.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationPingController {

	@GetMapping(value = "/product/status", produces = "text/plain", consumes = "text/plain")
	public String sayHello() {
		return "Financial Exchange Product Management service is alive\n";
	}
} 
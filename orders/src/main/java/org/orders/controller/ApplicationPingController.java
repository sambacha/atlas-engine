package org.atlas.engine.financialexchange.orders.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ApplicationPingController {

	@RequestMapping(value = "/orders/status", method = RequestMethod.GET) 
	public String sayHello() {
		return "Financial Exchange Order Management service is alive\n";
	}
} 
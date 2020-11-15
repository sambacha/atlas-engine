package org.atlas.engine.financialexchange.orderbooks.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ApplicationPingController {

	@RequestMapping(value = "/orderbook/status", method = RequestMethod.GET) 
	public String sayHello() {
		return "Financial Exchange Order Book service is alive\n";
	}
} 
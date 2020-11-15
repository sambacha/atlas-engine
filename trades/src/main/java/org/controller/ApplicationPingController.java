package org.atlas.engine.financialexchange.trades.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ApplicationPingController {

	@RequestMapping(value = "/trades/status", method = RequestMethod.GET) 
	public String sayHello() {
		return "Financial Exchange Trade service is alive\n";
	}
} 
package org.atlas.engine.financialexchange.participants.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ApplicationPingController {

	@RequestMapping(value = "/participant/status", method = RequestMethod.GET) 
	public String sayHello() {
		return "Financial Exchange Participant Management service is alive\n";
	}
} 
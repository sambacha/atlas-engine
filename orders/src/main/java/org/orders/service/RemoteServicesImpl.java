package org.atlas.engine.financialexchange.orders.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.atlas.engine.financialexchange.orders.domain.APIResponse;
import org.atlas.engine.financialexchange.orders.domain.Order;
import org.atlas.engine.financialexchange.orders.domain.OrderBookEntry;
import org.atlas.engine.financialexchange.orders.domain.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("remoteServicesImpl")
public class RemoteServicesImpl implements RemoteServices {

	private static final Logger logger = LoggerFactory.getLogger(RemoteServicesImpl.class);
		
	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate restTemplate;
	
	@Value("${spring.datasource.url")
	private String springDatasourceUrl;
	
	@Autowired
	private DiscoveryClient discoveryClient;

	private String getApiGatewayUrl() throws Exception {
		List<ServiceInstance> apiGateways = discoveryClient.getInstances("atlas-apigateway");
		if (apiGateways.isEmpty()) {
			throw new Exception("No API gateway hosts found from discovery service");
		}
		logger.trace("Discovered " + apiGateways.size() + " API gateways");
		URI apiGatewayUri = apiGateways.get(0).getUri();
		return apiGatewayUri.toString();
	}
	
	@Override
	public boolean isValidProduct(long productId) {
		boolean validProduct = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/product/equity/");
			stringBuffer.append(productId);
			String serviceUrl = stringBuffer.toString(); 
			System.out.println("The isValidProduct service url: " + serviceUrl);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(serviceUrl, HttpMethod.GET, entity, String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			int responseCode = root.get("responseCode").asInt();
			validProduct = responseCode == ResultCode.PRODUCT_FOUND.getCode();
		} catch (Exception e) {
			logger.error("Error while validating product", e);
		}
		return validProduct;
	}

	@Override
	public boolean isValidParticipant(long participantId) {
		boolean validParticipant = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/participant/broker/");
			stringBuffer.append(participantId);
			String serviceUrl = stringBuffer.toString();
			System.out.println("isValidParticipant service url: " + serviceUrl);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(serviceUrl, HttpMethod.GET, entity, String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			int responseCode = root.get("responseCode").asInt();
			validParticipant = responseCode == ResultCode.PARTICIPANT_FOUND.getCode();
		} catch (Exception e) {
			logger.error("Error while validating participant", e);
		}
		return validParticipant;
	}

	@Override
	public boolean cancelOrderInBook(long orderId) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean addOrderInBook(Order order) {
		logger.trace("Entering addOrderInBook: " + order);
		boolean addedToBook = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/orderBook/order/");
			String serviceUrl = stringBuffer.toString();
			System.out.println("addOrderInBook service url: " + serviceUrl);

			OrderBookEntry orderBookEntry = new OrderBookEntry();
			orderBookEntry.setEntryTime(order.getEntryTime());
			orderBookEntry.setOrderId(order.getId());
			orderBookEntry.setPrice(order.getPrice());
			orderBookEntry.setProductId(order.getProductId());
			orderBookEntry.setQuantity(order.getQuantity());
			orderBookEntry.setSide(order.getSide());
			orderBookEntry.setType(order.getType());
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<OrderBookEntry> entity = new HttpEntity<OrderBookEntry>(orderBookEntry, headers);
			
			ResponseEntity<APIResponse> response = restTemplate.exchange(serviceUrl, HttpMethod.POST, entity, APIResponse.class);
			addedToBook = response.getBody().getResponseCode() == ResultCode.ORDER_ACCEPTED.getCode();
		} catch (Exception e) {
			logger.error("Error during service call to orderbook service for order: " + order);
		}
		logger.trace("Exiting addOrderInBook: " + order);
		return addedToBook;
	}

	@Override
	public List<Long> getTradesForOrder(long orderId) {
		// TODO Auto-generated method stub
		return new ArrayList<Long>();
	}

}

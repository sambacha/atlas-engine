package org.atlas.engine.financialexchange.orderbooks.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.atlas.engine.financialexchange.orderbooks.domain.APIDataResponse;
import org.atlas.engine.financialexchange.orderbooks.domain.APIResponse;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderActivity;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderActivityEntry;
import org.atlas.engine.financialexchange.orderbooks.domain.ResultCode;
import org.atlas.engine.financialexchange.orderbooks.domain.TradeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
			
	@SuppressWarnings("rawtypes")
	@Override
	public boolean saveTrade(TradeEntry tradeEntry) {
		logger.trace("Entering saveTrade: "+ tradeEntry);
		boolean updatedTrade = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/trade/");
			String serviceUrl = stringBuffer.toString();
			System.out.println("saveTrade service url: " + serviceUrl);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<TradeEntry> entity = new HttpEntity<TradeEntry>(tradeEntry, headers);
			
			ResponseEntity<APIDataResponse> response = restTemplate.exchange(serviceUrl, HttpMethod.POST, entity, APIDataResponse.class);
			updatedTrade = response.getBody().getResponseCode() == ResultCode.TRADE_ACCEPTED.getCode();
		} catch (Exception e) {
			logger.error("Error while calling trade service for trade: "+ tradeEntry, e);
		}
		logger.trace("Exiting saveTrade: "+ tradeEntry);
		return updatedTrade;
	}

	@Override
	public String getProductSymbol(long productId) {
		String symbol = null;
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
			JsonNode product = root.get("data");
			symbol = product.get("symbol").asText();
		} catch (Exception e) {
			logger.error("Error while calling product service service for product id: "+ productId, e);
		}
		return symbol;
	}

	@Override
	public boolean updateOrders(TradeEntry tradeEntry) {
		logger.trace("Entering updateOrders: "+ tradeEntry);
		boolean updatedOrders = false;
		try {
			boolean buySideOrderUpdated = orderTradedQuantity(tradeEntry.getBuyTradableId(), 
					tradeEntry.getTradeTime(), tradeEntry.getQuantity());
			boolean sellSideOrderUpdated = orderTradedQuantity(tradeEntry.getSellTradableId(), 
					tradeEntry.getTradeTime(), tradeEntry.getQuantity());
			updatedOrders = buySideOrderUpdated && sellSideOrderUpdated;
		} catch (Exception e) {
			logger.error("Error while adding order traded quantity: "+ tradeEntry, e);
		}
		logger.trace("Exiting updateOrders: "+ tradeEntry);
		return updatedOrders;
	}
	
	@Override
	public boolean orderTradedQuantity(long orderId, LocalDateTime tradeTime, int tradedQuantity) {
		logger.trace("Entering orderTradedQuantity: orderId=" + orderId + "; traded=" + tradedQuantity);

		boolean updatedOrder = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/order/activity/");
			stringBuffer.append(orderId);
			String serviceUrl = stringBuffer.toString();
			System.out.println("orderTradedQuantity service url: " + serviceUrl);
			
			OrderActivityEntry orderActivityEntry = new OrderActivityEntry();
			orderActivityEntry.setOrderId(orderId);
			orderActivityEntry.setTradedQuantity(tradedQuantity);
			orderActivityEntry.setOrderActivity(OrderActivity.TRADED);
			orderActivityEntry.setActivityTime(tradeTime);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<OrderActivityEntry> entity = new HttpEntity<OrderActivityEntry>(orderActivityEntry, headers);
			
			ResponseEntity<APIResponse> response = restTemplate.exchange(serviceUrl, HttpMethod.PUT, entity, APIResponse.class);
			updatedOrder = response.getBody().getResponseCode() == ResultCode.ORDER_UPDATED.getCode();
		} catch (Exception e) {
			logger.error("Error in orderTradedQuantity: orderId=" + orderId + "; traded=" + tradedQuantity, e);
		}
		logger.trace("Exiting orderTradedQuantity: orderId=" + orderId + "; traded=" + tradedQuantity);

		return updatedOrder;
	}
	
	@Override
	public boolean orderBookedQuantity(long orderId, int bookedQuantity, LocalDateTime bookedTime) {
		logger.trace("Entering orderBookedQuantity: orderId=" + orderId + "; booked=" + bookedQuantity);

		boolean updatedOrder = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/order/activity/");
			stringBuffer.append(orderId);
			String serviceUrl = stringBuffer.toString();
			System.out.println("orderBookedQuantity service url: " + serviceUrl);

			OrderActivityEntry orderActivityEntry = new OrderActivityEntry();
			orderActivityEntry.setOrderId(orderId);
			orderActivityEntry.setBookedQuantity(bookedQuantity);
			orderActivityEntry.setOrderActivity(OrderActivity.BOOKED);
			orderActivityEntry.setActivityTime(bookedTime);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<OrderActivityEntry> entity = new HttpEntity<OrderActivityEntry>(orderActivityEntry, headers);
			
			ResponseEntity<APIResponse> response = restTemplate.exchange(serviceUrl, HttpMethod.PUT, entity, APIResponse.class);
			updatedOrder = response.getBody().getResponseCode() == ResultCode.ORDER_UPDATED.getCode();
		} catch (Exception e) {
			logger.error("Error in orderBookedQuantity: orderId=" + orderId + "; booked=" + bookedQuantity, e);
		}
		logger.trace("Exiting orderBookedQuantity: orderId=" + orderId + "; booked=" + bookedQuantity);

		return updatedOrder;
	}

	@Override
	public boolean orderCancelledQuantity(long orderId, int cancelledQuantity, LocalDateTime cancelledTime) {
		logger.trace("Entering orderCancelledQuantity: orderId=" + orderId + "; cancelled=" + cancelledQuantity);
		boolean updatedOrder = false;
		try {
			StringBuffer stringBuffer = new StringBuffer(getApiGatewayUrl());
			stringBuffer.append("/atlas/internal/api/order/activity/");
			stringBuffer.append(orderId);
			String serviceUrl = stringBuffer.toString();
			System.out.println("orderCancelledQuantity service url: " + serviceUrl);
			
			OrderActivityEntry orderActivityEntry = new OrderActivityEntry();
			orderActivityEntry.setOrderId(orderId);
			orderActivityEntry.setBookedQuantity(cancelledQuantity);
			orderActivityEntry.setOrderActivity(OrderActivity.CANCELLED);
			orderActivityEntry.setActivityTime(cancelledTime);
			
			RestTemplate restTemplate = new RestTemplate(); 
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<OrderActivityEntry> entity = new HttpEntity<OrderActivityEntry>(orderActivityEntry, headers);
			
			ResponseEntity<APIResponse> response = restTemplate.exchange(serviceUrl, HttpMethod.PUT, entity, APIResponse.class);
			updatedOrder = response.getBody().getResponseCode() == ResultCode.ORDER_UPDATED.getCode();
		} catch (Exception e) {
			logger.error("Exiting orderCancelledQuantity: orderId=" + orderId + "; cancelled=" + cancelledQuantity, e);
		}
		logger.trace("Exiting orderCancelledQuantity: orderId=" + orderId + "; cancelled=" + cancelledQuantity);
		return updatedOrder;
	}

}

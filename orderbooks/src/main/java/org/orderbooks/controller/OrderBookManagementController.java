package org.atlas.engine.financialexchange.orderbooks.controller;

import org.atlas.engine.financialexchange.orderbooks.domain.APIDataResponse;
import org.atlas.engine.financialexchange.orderbooks.domain.APIResponse;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookEntry;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookState;
import org.atlas.engine.financialexchange.orderbooks.domain.ResultCode;
import org.atlas.engine.financialexchange.orderbooks.monitor.ExecPosRecorder;
import org.atlas.engine.financialexchange.orderbooks.service.OrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderBookManagementController {

	//private static final Logger kLogger = LoggerFactory.getLogger(OrderBookManagementController.class);
	private static final Logger logger = LoggerFactory.getLogger(OrderBookManagementController.class);
	private static final String className = OrderBookManagementController.class.getSimpleName();

	@Autowired
	@Qualifier("orderBookServiceImpl")
	private OrderBookService orderBookService;
	
	@Autowired
	@Qualifier("asyncExecPosRecorder")
	private ExecPosRecorder execPosRecorder;
	
	@PostMapping(value = "/atlas/internal/api/orderBook/order", produces = "application/json", consumes = "application/json")
	public APIResponse acceptNewOrder(@RequestBody OrderBookEntry orderBookEntry) {
		logger.trace("Entering acceptNewOrder: " + orderBookEntry);
		//kLogger.trace("KafkaLog Entering OrderBookService acceptNewOrder");
		execPosRecorder.recordExecutionPoint(className, "acceptNewOrder", orderBookEntry.getOrderId(), "entry");
		APIResponse response = new APIResponse();
		try {
			orderBookService.addOrder(orderBookEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_ACCEPTED.getMessage());
			response.setResponseCode(ResultCode.ORDER_ACCEPTED.getCode());
		} catch (Throwable t) {
			logger.error("Unexpected error: " + orderBookEntry, t);
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		} finally {
			execPosRecorder.recordExecutionPoint(className, "acceptNewOrder", orderBookEntry.getOrderId(), "exit");
		}
		logger.trace("Exiting acceptNewOrder: " + orderBookEntry);
		//kLogger.trace("KafkaLog Exiting OrderBookService acceptNewOrder");
		return response;

	} 
	
	@GetMapping(value = "/atlas/internal/api/orderBook/{productId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<OrderBookState> getOrderBook(@PathVariable("productId") long productId) {
		APIDataResponse<OrderBookState> response = new APIDataResponse<>();
		try {
			OrderBookState orderReport = orderBookService.getOrderBookMontage(productId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_FOUND.getMessage());
			response.setData(orderReport);
			response.setResponseCode(ResultCode.ORDER_FOUND.getCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;

	} 

	@DeleteMapping(value = "/atlas/internal/api/orderBook/{orderId}", produces = "application/json", consumes = "application/json")
	public APIResponse deleteOrder(@PathVariable("orderId") long orderId, @RequestParam long productId) {
		APIResponse response = new APIResponse();
		try {
			orderBookService.cancelOrder(productId, orderId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_CANCELLED.getMessage());
			response.setResponseCode(ResultCode.ORDER_CANCELLED.getCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	} 

}

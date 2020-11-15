package org.atlas.engine.financialexchange.orders.controller;

import org.atlas.engine.financialexchange.orders.monitor.ExecPosRecorder;
import org.atlas.engine.financialexchange.orders.domain.APIDataResponse;
import org.atlas.engine.financialexchange.orders.domain.APIResponse;
import org.atlas.engine.financialexchange.orders.domain.ExchangeException;
import org.atlas.engine.financialexchange.orders.domain.OrderActivityEntry;
import org.atlas.engine.financialexchange.orders.domain.OrderEntry;
import org.atlas.engine.financialexchange.orders.domain.OrderReport;
import org.atlas.engine.financialexchange.orders.domain.ResultCode;
import org.atlas.engine.financialexchange.orders.service.OrderManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderManagementController {

	private static final Logger logger = LoggerFactory.getLogger(OrderManagementController.class);
	//private final static Log logger = LogFactory.getLog(OrderManagementController.class);
	private final static String className = OrderManagementController.class.getSimpleName();

	@Autowired
	@Qualifier("orderManagementServiceImpl")
	private OrderManagementService orderManagementService;
	
	@Autowired
	@Qualifier("asyncExecPosRecorder")
	private ExecPosRecorder execPosRecorder;

	@PostMapping(value = "/finex/api/order", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Long> acceptNewOrder(@RequestBody OrderEntry orderEntry) {
		execPosRecorder.recordExecutionPoint(className, "acceptNewOrder", -1, "entry");
		logger.trace("Entering acceptNewOrder for POST on /order");
		APIDataResponse<Long> response = new APIDataResponse<Long>();
		long orderId = -1;
		try {
			orderId = orderManagementService.acceptNewOrder(orderEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_ACCEPTED.getMessage());
			response.setResponseCode(ResultCode.ORDER_ACCEPTED.getCode());
			response.setData(orderId);
			logger.debug("Order added: " + orderEntry);
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		logger.trace("Exiting acceptNewOrder for POST on /order");
		execPosRecorder.recordExecutionPoint(className, "acceptNewOrder", orderId, "exit");
		return response;
	} 
	
	@PutMapping(value = "/finex/api/order/{orderId}", produces = "application/json", consumes = "application/json")
	public APIResponse updateOrder(@PathVariable("orderId") long orderId,
			@RequestBody OrderEntry orderEntry) {
		execPosRecorder.recordExecutionPoint(className, "updateOrder", orderId, "entry");
		APIResponse response = new APIResponse();
		try {
			orderManagementService.updateOrder(orderId, orderEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_UPDATED.getMessage());
			response.setResponseCode(ResultCode.ORDER_UPDATED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		execPosRecorder.recordExecutionPoint(className, "updateOrder", orderId, "exit");
		return response;

	} 
	
	@PutMapping(value = "/finex/internal/api/order/activity/{orderId}", produces = "application/json", consumes = "application/json")
	public APIResponse updateOrderTrade(@PathVariable("orderId") long orderId,
			@RequestBody OrderActivityEntry orderActivityEntry) {
		execPosRecorder.recordExecutionPoint(className, "updateOrderTrade", orderId, "entry");
		APIResponse response = new APIResponse();
		try {
			orderManagementService.addOrderActivity(orderId, orderActivityEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_UPDATED.getMessage());
			response.setResponseCode(ResultCode.ORDER_UPDATED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		execPosRecorder.recordExecutionPoint(className, "updateOrderTrade", orderId, "exit");
		return response;

	} 


	@GetMapping(value = "/finex/api/order/{orderId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<OrderReport> getOrderStatus(@PathVariable("orderId") long orderId) {
		execPosRecorder.recordExecutionPoint(className, "getOrderStatus", orderId, "entry");
		APIDataResponse<OrderReport> response = new APIDataResponse<>();
		try {
			OrderReport orderReport = orderManagementService.getOrderStatus(orderId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_FOUND.getMessage());
			response.setData(orderReport);
			response.setResponseCode(ResultCode.ORDER_FOUND.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		execPosRecorder.recordExecutionPoint(className, "getOrderStatus", orderId, "exit");
		return response;
	} 

	@DeleteMapping(value = "/finex/api/order/{orderId}", produces = "application/json", consumes = "application/json")
	public APIResponse deleteOrder(@PathVariable("orderId") long orderId) {
		execPosRecorder.recordExecutionPoint(className, "deleteOrder", orderId, "entry");
		APIResponse response = new APIResponse();
		try {
			orderManagementService.cancelOrder(orderId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.ORDER_CANCELLED.getMessage());
			response.setResponseCode(ResultCode.ORDER_CANCELLED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		execPosRecorder.recordExecutionPoint(className, "deleteOrder", orderId, "exit");
		return response;
	} 
}
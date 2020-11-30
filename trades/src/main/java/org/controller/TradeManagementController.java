package org.atlas.engine.financialexchange.trades.controller;

import java.util.List;

import org.atlas.engine.financialexchange.trades.domain.APIDataResponse;
import org.atlas.engine.financialexchange.trades.domain.APIResponse;
import org.atlas.engine.financialexchange.trades.domain.ExchangeException;
import org.atlas.engine.financialexchange.trades.domain.ResultCode;
import org.atlas.engine.financialexchange.trades.domain.TradeEntry;
import org.atlas.engine.financialexchange.trades.domain.TradeReport;
import org.atlas.engine.financialexchange.trades.service.TradeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TradeManagementController {

	@Autowired
	@Qualifier("tradeManagementServiceImpl")
	private TradeManagementService tradeManagmentService;
	
	@PostMapping(value = "/atlas/internal/api/trade", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Long> addTradeInternal(@RequestBody TradeEntry tradeEntry)  {
		APIDataResponse<Long> response = new APIDataResponse<>();
		try {
			long tradeId = tradeManagmentService.acceptTrade(tradeEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.TRADE_ACCEPTED.getMessage());
			response.setData(tradeId);
			response.setResponseCode(ResultCode.TRADE_ACCEPTED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}
	 
	@GetMapping(value = "/atlas/internal/api/trade/{tradeId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<TradeReport> getTradeInternal(@PathVariable("tradeId") long tradeId)  {
		APIDataResponse<TradeReport> response = getTradeImpl(tradeId);
		return response;
	}

	@GetMapping(value = "/atlas/api/trade/{tradeId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<TradeReport> getTradePublic(@PathVariable("tradeId") long tradeId)  {
		APIDataResponse<TradeReport> response = getTradeImpl(tradeId);
		return response;
	}

	@GetMapping(value = "/atlas/internal/api/trade/order/{orderId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<List<TradeReport>> getTradesForOrderInternal(@PathVariable("orderId") long orderId)  {
		APIDataResponse<List<TradeReport>> response = getTradesForOrderImpl(orderId);
		return response;
	}

	@GetMapping(value = "/atlas/api/trade/order/{orderId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<List<TradeReport>> getTradesForOrderPublic(@PathVariable("orderId") long orderId)  {
		APIDataResponse<List<TradeReport>> response = getTradesForOrderImpl(orderId);
		return response;
	}

	@DeleteMapping(value = "/atlas/internal/api/trade/{tradeId}", produces = "application/json", consumes = "application/json")
	public APIResponse bustTrade(@PathVariable("tradeId") long tradeId)  {
		APIResponse response = new APIResponse();
		try {
			tradeManagmentService.bustTrade(tradeId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.TRADE_BUSTED.getMessage());
			response.setResponseCode(ResultCode.TRADE_BUSTED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}
	
	public APIDataResponse<TradeReport> getTradeImpl(long tradeId)  {
		APIDataResponse<TradeReport> response = new APIDataResponse<>();
		try {
			TradeReport tradeReport = tradeManagmentService.getTrade(tradeId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.TRADE_FOUND.getMessage());
			response.setData(tradeReport);
			response.setResponseCode(ResultCode.TRADE_FOUND.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}

	public APIDataResponse<List<TradeReport>> getTradesForOrderImpl(long orderId)  {
		APIDataResponse<List<TradeReport>> response = new APIDataResponse<>();
		try {
			List<TradeReport> trades = tradeManagmentService.getTradesForOrder(orderId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.TRADE_FOUND.getMessage());
			response.setData(trades);
			response.setResponseCode(ResultCode.TRADE_FOUND.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}

}

package org.atlas.engine.financialexchange.products.controller;

import org.atlas.engine.financialexchange.products.domain.APIDataResponse;
import org.atlas.engine.financialexchange.products.domain.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.atlas.engine.financialexchange.products.domain.Equity;
import org.atlas.engine.financialexchange.products.domain.ExchangeException;
import org.atlas.engine.financialexchange.products.domain.ProductEntry;
import org.atlas.engine.financialexchange.products.domain.ResultCode;
import org.atlas.engine.financialexchange.products.service.ProductManagementService;

@RestController
public class ProductManagementController {

	@Value("spring.datasource.url")
	String springDataSourceUrl;
	
	@Value("spring.datasource.username")
	String springDatasourceUsername;
	
	@Autowired
	@Qualifier("productManagementServiceImpl")
	private ProductManagementService productManagmentService;
		
	@PostMapping(value = "/atlas/internal/api/product/equity", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Long> addEquity(@RequestBody ProductEntry productEntry)  {
		APIDataResponse<Long> response = new APIDataResponse<>();
		try {
			long productId = productManagmentService.addProduct(productEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PRODUCT_ADDED.getMessage());
			response.setData(productId);
			response.setResponseCode(ResultCode.PRODUCT_ADDED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}
	 
	@PutMapping(value = "/atlas/internal/api/product/equity/{productId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Equity> updateEquity(@PathVariable("productId") long productId,
			@RequestBody ProductEntry productEntry)  {
		APIDataResponse<Equity> response = new APIDataResponse<>();
		try {
			Equity equity = (Equity) productManagmentService.updateProduct(productId, productEntry);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PRODUCT_UPDATED.getMessage());
			response.setData(equity);
			response.setResponseCode(ResultCode.PRODUCT_UPDATED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}

	@GetMapping(value = "/atlas/internal/api/product/equity", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Equity> getEquityBySymbolInternal(@RequestParam("symbol") String symbol)  {
		APIDataResponse<Equity> response = getEquityBySymbolImpl(symbol);
		return response;
	}

	@GetMapping(value = "/atlas/api/product/equity", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Equity> getEquityBySymbolPublic(@RequestParam("symbol") String symbol)  {
		APIDataResponse<Equity> response = getEquityBySymbolImpl(symbol);
		return response;
	}

	@GetMapping(value = "/atlas/internal/api/product/equity/{productId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Equity> getEquityByProudctIdInternal(@PathVariable("productId") long productId)  {
		APIDataResponse<Equity> response = getEquityByProductIdImpl(productId);
		return response;
	}

	@GetMapping(value = "/atlas/api/product/equity/{productId}", produces = "application/json", consumes = "application/json")
	public APIDataResponse<Equity> getEquityByProudctIdPublic(@PathVariable("productId") long productId)  {
		APIDataResponse<Equity> response = getEquityByProductIdImpl(productId);
		return response;
	}

	@DeleteMapping(value = "/atlas/internal/api/product/equity/{productId}", produces = "application/json", consumes = "application/json")
	public APIResponse deleteEquity(@PathVariable("productId") long productId)  {
		APIResponse response = new APIResponse();
		try {
			productManagmentService.deleteProduct(productId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PRODUCT_DELETED.getMessage());
			response.setResponseCode(ResultCode.PRODUCT_DELETED.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}

	private APIDataResponse<Equity> getEquityBySymbolImpl(String symbol)  {
		APIDataResponse<Equity> response = new APIDataResponse<>();
		try {
			Equity equity = (Equity) productManagmentService.getProduct(symbol);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PRODUCT_FOUND.getMessage());
			response.setData(equity);
			response.setResponseCode(ResultCode.PRODUCT_FOUND.getCode());
		} catch (ExchangeException ee) {
			response.setErrorMessage(ee.getMessage());
			response.setResponseCode(ee.getErrorCode());
		} catch (Throwable t) {
			response.setErrorMessage("Unexpected error. Please contact customer service");
			response.setResponseCode(ResultCode.GENERAL_ERROR.getCode());
		}
		return response;
	}

	private APIDataResponse<Equity> getEquityByProductIdImpl(long productId)  {
		APIDataResponse<Equity> response = new APIDataResponse<>();
		try {
			Equity equity = (Equity) productManagmentService.getProduct(productId);
			response.setSuccess(true);
			response.setInfoMessage(ResultCode.PRODUCT_FOUND.getMessage());
			response.setData(equity);
			response.setResponseCode(ResultCode.PRODUCT_FOUND.getCode());
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

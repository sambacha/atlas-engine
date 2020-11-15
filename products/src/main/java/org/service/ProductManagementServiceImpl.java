package org.atlas.engine.financialexchange.products.service;

import org.atlas.engine.financialexchange.products.domain.Equity;
import org.atlas.engine.financialexchange.products.domain.ExchangeException;
import org.atlas.engine.financialexchange.products.domain.Product;
import org.atlas.engine.financialexchange.products.domain.ProductEntry;
import org.atlas.engine.financialexchange.products.domain.ResultCode;
import org.atlas.engine.financialexchange.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("productManagementServiceImpl")
public class ProductManagementServiceImpl implements ProductManagementService {

	@Autowired
	@Qualifier("productJpaRepositoryImpl")
	ProductRepository productRepository;
	
	@Override
	public long addProduct(ProductEntry productEntry) throws ExchangeException {
		Product product = null;
		if (productEntry.getProductType().equals("EQUITY")) {
			product = new Equity();
			product.setSymbol(productEntry.getSymbol());
			product.setDescription(productEntry.getDescription());
			productRepository.saveProduct(product);
		} else {
			throw new ExchangeException(ResultCode.UNSUPPORTED_ENTITY);
		}
		return product.getId();
	}

	@Override
	public void deleteProduct(long productId) throws ExchangeException {
		boolean deleted = productRepository.deleteProduct(productId);
		if (!deleted) {
			Product product = productRepository.getProduct(productId);
			if (product == null) {
				throw new ExchangeException(ResultCode.PRODUCT_NOT_FOUND);
			}
		}
	}

	@Override
	public Product updateProduct(long productId, ProductEntry productEntry) throws ExchangeException {
		Product product = null;
		if (productEntry.getProductType().equals("EQUITY")) {
			product = (Equity) productRepository.getProduct(productId);
			if (product == null) {
				throw new ExchangeException(ResultCode.PRODUCT_NOT_FOUND);
			}
			product.setSymbol(productEntry.getSymbol());
			product.setDescription(productEntry.getDescription());
			product = (Equity) productRepository.getProduct(productId);
		} else {
			throw new ExchangeException(ResultCode.UNSUPPORTED_ENTITY);
		}
		return product;
	}

	@Override
	public Product getProduct(long productId) throws ExchangeException {
		Product product = productRepository.getProduct(productId);
		if (product == null) {
			throw new ExchangeException(ResultCode.PRODUCT_NOT_FOUND);
		}
		return product;
	}

	@Override
	public Product getProduct(String symbol) throws ExchangeException {
		Product product = productRepository.getProduct(symbol);
		if (product == null) {
			throw new ExchangeException(ResultCode.PRODUCT_NOT_FOUND);
		}
		return product;
	}

}


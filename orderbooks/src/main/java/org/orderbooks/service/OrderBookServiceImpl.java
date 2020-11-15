package org.atlas.engine.financialexchange.orderbooks.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.atlas.engine.financialexchange.orderbooks.domain.ExchangeException;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookEntry;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookItem;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookItemImpl;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookState;
import org.atlas.engine.financialexchange.orderbooks.monitor.ExecPosRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("orderBookServiceImpl")
public class OrderBookServiceImpl implements OrderBookService {

	private final static String className = OrderBookServiceImpl.class.getSimpleName();
	
	@Autowired
	@Qualifier("remoteServicesImpl")
	private RemoteServices remoteServices;
	
	@Autowired
	@Qualifier("asyncExecPosRecorder")
	private ExecPosRecorder execPosRecorder;
	
	private Map<Long, OrderBook> orderBooks;
	
	public OrderBookServiceImpl() {
		this.orderBooks = new ConcurrentHashMap<Long, OrderBook>();
	}
	
	public OrderBook getOrderBook(long productId) throws ExchangeException {
		OrderBook orderBook = null;
		orderBook = orderBooks.get(productId);
		if (orderBook == null) {
			String productSymbol = remoteServices.getProductSymbol(productId);
			orderBook = new OrderBookImpl(remoteServices, execPosRecorder, productId, productSymbol);
			orderBooks.put(productId, orderBook);
		}
		return orderBook;
	}
		
	public void deleteOrderBook(long productId) {
		orderBooks.remove(productId);
	}

	public void addOrder(OrderBookEntry orderBookEntry) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "addOrder", orderBookEntry.getOrderId(), "entry");
		OrderBook orderBook = getOrderBook(orderBookEntry.getProductId());
		OrderBookItem orderBookItem = createOrderBookItem(orderBookEntry);
		orderBook.processOrder(orderBookItem);
		execPosRecorder.recordExecutionPoint(className, "addOrder", orderBookEntry.getOrderId(), "exit");
	}

	@Override
	public void cancelOrder(long productId, long orderId) throws ExchangeException {
		OrderBook orderBook = getOrderBook(productId);
		orderBook.cancelOrder(orderId);		
	}

	@Override
	public OrderBookState getOrderBookMontage(long productId) {
		// TODO Auto-generated method stub
		return null;
	}

	private OrderBookItem createOrderBookItem(OrderBookEntry orderBookEntry) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "createOrderBookItem", orderBookEntry.getOrderId(), "entry");
		OrderBookItem order = new OrderBookItemImpl();
		order.setOrderId(orderBookEntry.getOrderId());
		order.setProductId(orderBookEntry.getProductId());
		order.setType(orderBookEntry.getType());
		order.setSide(orderBookEntry.getSide());
		order.setQuantity(orderBookEntry.getQuantity());
		order.setPrice(orderBookEntry.getPrice());
		order.setEntryTime(orderBookEntry.getEntryTime());
		execPosRecorder.recordExecutionPoint(className, "createOrderBookItem", orderBookEntry.getOrderId(), "exit");
		return order;
	}

}


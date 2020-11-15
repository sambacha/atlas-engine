package org.atlas.engine.financialexchange.orders.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.atlas.engine.financialexchange.orders.domain.ExchangeException;
import org.atlas.engine.financialexchange.orders.domain.Order;
import org.atlas.engine.financialexchange.orders.domain.OrderActivity;
import org.atlas.engine.financialexchange.orders.domain.OrderActivityEntry;
import org.atlas.engine.financialexchange.orders.domain.OrderEntry;
import org.atlas.engine.financialexchange.orders.domain.OrderImpl;
import org.atlas.engine.financialexchange.orders.domain.OrderLongevity;
import org.atlas.engine.financialexchange.orders.domain.OrderReport;
import org.atlas.engine.financialexchange.orders.domain.OrderStatus;
import org.atlas.engine.financialexchange.orders.domain.ResultCode;
import org.atlas.engine.financialexchange.orders.monitor.ExecPosRecorder;
import org.atlas.engine.financialexchange.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("orderManagementServiceImpl")
public class OrderManagementServiceImpl implements OrderManagementService {

	//private static final Log logger = LogFactory.getLog(OrderManagementServiceImpl.class);
	private static final Logger logger = LoggerFactory.getLogger(OrderManagementServiceImpl.class);
	private static final String className = OrderManagementServiceImpl.class.getSimpleName();
	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");
	
	@Autowired
	@Qualifier("orderJpaRepositoryImpl")
	private OrderRepository orderRepository;
	
	@Autowired
	@Qualifier("remoteServicesImpl")
	private RemoteServices remoteServices;
	
	@Autowired
	@Qualifier("asyncExecPosRecorder")
	private ExecPosRecorder execPosRecorder;

	@Override
	public void cancelOrder(long orderId) throws ExchangeException {
		Order order = orderRepository.getOrder(orderId);
		if (order == null) {
			throw new ExchangeException(ResultCode.ORDER_NOT_FOUND);
		}
		if (order.getStatus() == OrderStatus.FILLED) {
			throw new ExchangeException(ResultCode.ORDER_FILLED);
		}
		remoteServices.cancelOrderInBook(orderId);
	}

	@Override
	public OrderReport getOrderStatus(long orderId) throws ExchangeException {
		Order order = orderRepository.getOrder(orderId);
		OrderReport orderReport = createOrderReport(order);
		return orderReport;
	}

	@Override
	public long acceptNewOrder(OrderEntry orderEntry) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "acceptNewOrder", orderEntry.getId(), "entry");
		logger.trace("Entering acceptOrder");
		Order order = createOrder(orderEntry);
		long orderId = orderRepository.saveOrder((OrderImpl) order);
		if (orderId < 0) {
			throw new ExchangeException(ResultCode.GENERAL_ERROR);
		}
		order.setId(orderId);
		remoteServices.addOrderInBook(order);
		logger.trace("Exiting acceptOrder");
		execPosRecorder.recordExecutionPoint(className, "acceptNewOrder", orderEntry.getId(), "exit");
		return order.getId();
	}

	private Order createOrder(OrderEntry orderEntry) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "creatOrder", orderEntry.getId(), "entry");
		logger.trace("Entering createOrder");
		if (!remoteServices.isValidProduct(orderEntry.getProductId())) {
			throw new ExchangeException(ResultCode.PRODUCT_NOT_FOUND);
		}
		
		if (!remoteServices.isValidParticipant(orderEntry.getParticipantId())) {
			throw new ExchangeException(ResultCode.PARTICIPANT_NOT_FOUND);
		}
		
		Order order = new OrderImpl();
		order.setEntryTime(LocalDateTime.now());
		order.setProductId(orderEntry.getProductId());
		order.setParticipantId(orderEntry.getParticipantId());
		order.setType(orderEntry.getType());
		order.setLongevity(OrderLongevity.DAY);
		order.setSide(orderEntry.getSide());
		order.setQuantity(orderEntry.getQuantity());
		order.setPrice(orderEntry.getPrice());
		order.setStatus(OrderStatus.NEW);
		
		logger.trace("Exiting createOrder");
		execPosRecorder.recordExecutionPoint(className, "creatOrder", orderEntry.getId(), "exit");

		return order;
	}

	@Override
	public void updateOrder(long orderId, OrderEntry orderEntry) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "updateOrder", orderEntry.getId(), "entry");
		Order order = createOrder(orderEntry);
		orderRepository.saveOrder((OrderImpl) order);
		execPosRecorder.recordExecutionPoint(className, "updateOrder", orderEntry.getId(), "exit");
	}

	private OrderReport createOrderReport(Order order) {
		execPosRecorder.recordExecutionPoint(className, "createOrderReport", order.getId(), "entry");
		OrderReport orderReport = new OrderReport();
		orderReport.setBookedQuantity(order.getBookedQuantity());
		orderReport.setEntryTime(timeFormatter.format(order.getEntryTime()));
		orderReport.setId(order.getId());
		orderReport.setParticipantId(order.getParticipantId());
		orderReport.setLongevity(order.getLongevity());
		orderReport.setOriginalQuantity(order.getQuantity());
		orderReport.setProductId(order.getProductId());
		orderReport.setSide(order.getSide());
		orderReport.setStatus(order.getStatus());
		orderReport.setTradedQuantity(order.getTradedQantity());
		orderReport.setType(order.getType());
		orderReport.setPrice(order.getPrice());
		List<Long> trades = remoteServices.getTradesForOrder(order.getId());
		orderReport.setTrades(trades);
		execPosRecorder.recordExecutionPoint(className, "createOrderReport", order.getId(), "exit");
		return orderReport;
	}

	@Override
	public List<OrderReport> getOrdersForProduct(long productId) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "getOrdersForProduct", productId, "entry");
		List<Order> orders = orderRepository.getOrdersByProduct(productId);
		List<OrderReport> orderReports = orders.stream()
				.map(this::createOrderReport)
				.collect(Collectors.toList());
		execPosRecorder.recordExecutionPoint(className, "getOrdersForProduct", productId, "exit");
		return orderReports;
	}

	@Override
	public void addOrderActivity(long orderId, OrderActivityEntry orderActivityEntry) throws ExchangeException {
		execPosRecorder.recordExecutionPoint(className, "addOrderActivity", orderId, "entry");
		logger.trace("Entering addOrderActivity with activity: " + orderActivityEntry);
		Order order = orderRepository.getOrder(orderId);
		logger.debug("Order activity for order: " + order);
		if (order == null) {
			throw new ExchangeException(ResultCode.ORDER_NOT_FOUND);
		}
		if (orderActivityEntry.getOrderActivity() == OrderActivity.BOOKED) {
			if (order.getTradedQantity() > 0) {
				order.setStatus(OrderStatus.PARTIALLY_BOOKED_FILLED);
			} else {
				order.setStatus(OrderStatus.BOOKED);
			}
			order.setBookedQuantity(orderActivityEntry.getBookedQuantity());
		} else if (orderActivityEntry.getOrderActivity() == OrderActivity.TRADED) {
			if (order.getStatus() == OrderStatus.BOOKED || order.getStatus() == OrderStatus.PARTIALLY_BOOKED_FILLED) {
				order.setBookedQuantity(order.getBookedQuantity() - orderActivityEntry.getTradedQuantity());
			}
			order.setTradedQuantity(order.getTradedQantity() + orderActivityEntry.getTradedQuantity());
			if (order.getBookedQuantity() > 0) {
				order.setStatus(OrderStatus.PARTIALLY_BOOKED_FILLED);
			} else {
				order.setStatus(OrderStatus.FILLED);
			}
		} else if (orderActivityEntry.getOrderActivity() == OrderActivity.CANCELLED) {
			order.setCancelledQuantity(orderActivityEntry.getCancelledQuantity());
			order.setBookedQuantity(0);
			if (order.getTradedQantity() > 0) {
				order.setStatus(OrderStatus.PARTIALLY_CANCELLED);
			} else {
				order.setStatus(OrderStatus.CANCELLED);
			}
		} else {
			order.setStatus(OrderStatus.UNKNOWN);
		}
		orderRepository.saveOrder((OrderImpl) order);
		logger.trace("Exiting addOrderActivity with activity: " + orderActivityEntry);
		execPosRecorder.recordExecutionPoint(className, "addOrderActivity", orderId, "exit");
	}

}

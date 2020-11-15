package org.atlas.engine.financialexchange.orderbooks.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.atlas.engine.financialexchange.orderbooks.domain.OrderBookItem;
import org.atlas.engine.financialexchange.orderbooks.domain.OrderType;
import org.atlas.engine.financialexchange.orderbooks.domain.Side;
import org.atlas.engine.financialexchange.orderbooks.domain.TradeEntry;
import org.atlas.engine.financialexchange.orderbooks.monitor.ExecPosRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderBookImpl implements OrderBook {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderBookImpl.class);
	private static final String className = OrderBookImpl.class.getSimpleName();
	
	private static BigDecimal TWO = new BigDecimal("2.0");
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");
	
	private RemoteServices remoteServices;
	private ExecPosRecorder execPosRecorder;

	private long productId;
	private String productSymbol;
	private List<OrderBookItem> items = new ArrayList<OrderBookItem>();
	private BigDecimal tickSize;
	
	public OrderBookImpl(RemoteServices remoteServices, ExecPosRecorder execPosRecorder, long productId, String productSymbol) {
		this(remoteServices, execPosRecorder, new BigDecimal("0.01"), productId, productSymbol);
	}

	public OrderBookImpl(RemoteServices remoteServices, ExecPosRecorder execPosRecorder, BigDecimal tickSize, long productId, String productSymbol) {
		this.tickSize = tickSize;
		this.setProductId(productId);
		this.setProductSymbol(productSymbol);
		this.remoteServices = remoteServices;
		this.execPosRecorder = execPosRecorder;
	}
	
	/**
	 * Clips price according to tickSize
	 * 
	 * @param price
	 * @return
	 */
	private BigDecimal clipPrice(BigDecimal price) {
		int numDecPlaces = (int) Math.log10(1 / this.tickSize.doubleValue());
		BigDecimal rounded = price.setScale(numDecPlaces, RoundingMode.HALF_UP);
		return rounded;
	}
	
	
	public void processOrder(OrderBookItem incomingOrder) {	
		execPosRecorder.recordExecutionPoint(className, "processOrder", incomingOrder.getOrderId(), "entry");
		if (incomingOrder.getQuantity() <= 0 ) {
			throw new IllegalArgumentException("processOrder() given qty <= 0");
		}
		
		if (!(incomingOrder.getSide() == Side.BUY || incomingOrder.getSide() == Side.SELL)) {		
			throw new IllegalArgumentException("order neither market nor limit: " + 
					incomingOrder.getSide());
		}
		
		if (incomingOrder.getType() == OrderType.LIMIT) {
			BigDecimal clippedPrice = clipPrice(incomingOrder.getPrice());
			incomingOrder.setPrice(clippedPrice);
			processLimitOrder(incomingOrder);
		} else {
			processMarketOrder(incomingOrder);
		}
		execPosRecorder.recordExecutionPoint(className, "processOrder", incomingOrder.getOrderId(), "exit");
	}
	
	
	private void processMarketOrder(OrderBookItem incomingOrder) {
		execPosRecorder.recordExecutionPoint(className, "processMarketOrder", incomingOrder.getOrderId(), "entry");
		Side side = incomingOrder.getSide();
		int qtyRemaining = incomingOrder.getQuantity();
		if (side == Side.BUY) {
			List<OrderBookItem> offersByBestPrice = this.items.stream()
					.filter(o -> o.getSide() == Side.SELL && o.getType() != OrderType.MARKET)
					.sorted(Comparator.comparing(OrderBookItem::getPrice)
							.thenComparing(OrderBookItem::getEntryTime))
					.collect(Collectors.toList());
			while ((qtyRemaining > 0) && (offersByBestPrice.size() > 0)) {
				// Find the best offer to match the buy order 
				// That would be to find the sell orders asking for smallest price.
				//List<Order> ordersAtMinPrice = offers.stream().sorted(Comparators.)
				qtyRemaining = processOrderList(offersByBestPrice, qtyRemaining,
												incomingOrder);
			}
		} else if (side == Side.SELL) {
			List<OrderBookItem> bidsByBestPrice = this.items.stream()
					.filter(o -> o.getSide() == Side.BUY && o.getType() != OrderType.MARKET)
					.sorted(Comparator.comparing(OrderBookItem::getPrice)
							.reversed()
							.thenComparing(OrderBookItem::getEntryTime))
					.collect(Collectors.toList());
			while ((qtyRemaining > 0) && (bidsByBestPrice.size() > 0)) {
				// Find the best offer to match the buy order 
				// That would be to find the sell orders asking for smallest price.
				//List<Order> ordersAtMinPrice = offers.stream().sorted(Comparators.)
				qtyRemaining = processOrderList(bidsByBestPrice, qtyRemaining,
												incomingOrder);
			}
		} else {
			throw new IllegalArgumentException("order neither market nor limit: " + 
				    						    side);
		}
		if (qtyRemaining > 0) {
			remoteServices.orderCancelledQuantity(incomingOrder.getOrderId(), qtyRemaining, LocalDateTime.now());
		}
		incomingOrder.setQuantity(qtyRemaining);
		
//		if (incomingOrder.getTradedQantity() > 0 && incomingOrder.getTradedQantity() < incomingOrder.getQuantity()) {
//			incomingOrder.setStatus(OrderStatus.PARTIALLY_FILLED);
//		} else if (incomingOrder.getTradedQantity() == incomingOrder.getQuantity()) {
//			incomingOrder.setStatus(OrderStatus.FILLED);
//		} else {
//			incomingOrder.setStatus(OrderStatus.NOT_FILLED);
//		}
//		remoteServices.saveOrder(incomingOrder);
		execPosRecorder.recordExecutionPoint(className, "processMarketOrder", incomingOrder.getOrderId(), "exit");

	}
	
	
	private void processLimitOrder(OrderBookItem incomingOrder) {
		execPosRecorder.recordExecutionPoint(className, "processLimitOrder", incomingOrder.getOrderId(), "entry");
		logger.trace("Entering procesLimitOrder: " + incomingOrder);
		int qtyRemaining = incomingOrder.getQuantity();
		List<OrderBookItem> tradableOrders = this.items.stream()
				.filter(o -> {
					boolean match = false;
					if (incomingOrder.getSide() == Side.BUY) {
						match = o.getSide() == Side.SELL && incomingOrder.getPrice().compareTo(o.getPrice()) >= 0;
					} else {
						match = o.getSide() == Side.BUY && incomingOrder.getPrice().compareTo(o.getPrice()) <= 0;
					}
					return match;
				 })
				.sorted(Comparator.comparing(OrderBookItem::getEntryTime))
				.collect(Collectors.toList());

		while ((tradableOrders.size() > 0) && 
				(qtyRemaining > 0)) {
			qtyRemaining = processOrderList(tradableOrders, qtyRemaining, incomingOrder);
		}
		if (qtyRemaining > 0) {
			incomingOrder.setQuantity(qtyRemaining);
			remoteServices.orderBookedQuantity(incomingOrder.getOrderId(), incomingOrder.getQuantity(), LocalDateTime.now());
			this.items.add(incomingOrder);
		}
		
		List<OrderBookItem> tradedOrders = items.stream()
				.filter(o -> o.getQuantity() == 0)
				.collect(Collectors.toList());
		tradedOrders.stream().forEach(o -> items.remove(o)); 
		
//		incomingOrder.setBookedQuantity(incomingOrder.getQuantity() - incomingOrder.getTradedQantity());
//		if (incomingOrder.getTradedQantity() > 0 && incomingOrder.getBookedQuantity() > 0) {
//			incomingOrder.setStatus(OrderStatus.PARTIALLY_BOOKED_FILLED);
//		} else if (incomingOrder.getTradedQantity() == 0 && incomingOrder.getBookedQuantity() > 0) {
//			incomingOrder.setStatus(OrderStatus.BOOKED);
//		} else if (incomingOrder.getTradedQantity() == incomingOrder.getQuantity()) {
//			incomingOrder.setStatus(OrderStatus.FILLED);
//		} else {
//			incomingOrder.setStatus(OrderStatus.UNKNOWN);
//		}
//		if (incomingOrder.getBookedQuantity() > 0) {
//			this.orders.add(incomingOrder);
//		}
//		remoteServices.saveOrder(incomingOrder);
		logger.trace("Exiting procesLimitOrder: " + incomingOrder);
		execPosRecorder.recordExecutionPoint(className, "processLimitOrder", incomingOrder.getOrderId(), "exit");
	}
	
	
	private int processOrderList(List<OrderBookItem> tradableOrders, int qtyRemaining, OrderBookItem incomingOrder) {
		execPosRecorder.recordExecutionPoint(className, "processOrderList", incomingOrder.getOrderId(), "entry");
		logger.trace("Entering processOrderList: " + incomingOrder);

		Iterator<OrderBookItem> iterator = tradableOrders.iterator();
		
		while ((iterator.hasNext()) && (qtyRemaining > 0)) {
			int qtyTraded = 0;
			OrderBookItem headOrder = iterator.next();
			if (qtyRemaining < headOrder.getQuantity()) {
				qtyTraded = qtyRemaining;
				qtyRemaining = 0;
				headOrder.setQuantity(headOrder.getQuantity() - qtyTraded);
//				headOrder.setStatus(OrderStatus.PARTIALLY_BOOKED_FILLED);
			} else {
				qtyTraded = headOrder.getQuantity();
				qtyRemaining -= qtyTraded;
				headOrder.setQuantity(0);
//				headOrder.setStatus(OrderStatus.FILLED);
				iterator.remove();
			}
//			headOrder.setTradedQuantity(headOrder.getTradedQantity() + qtyTraded);
//			headOrder.setBookedQuantity(headOrder.getBookedQuantity() - qtyTraded);
//			incomingOrder.setTradedQuantity(incomingOrder.getTradedQantity() + qtyTraded);
			TradeEntry tradeEntry = new TradeEntry();
			if (incomingOrder.getSide() == Side.SELL) {
				tradeEntry.setSellTradableId(incomingOrder.getOrderId());
				tradeEntry.setBuyTradableId(headOrder.getOrderId());
			} else {
				tradeEntry.setSellTradableId(headOrder.getOrderId());
				tradeEntry.setBuyTradableId(incomingOrder.getOrderId());
			}
			tradeEntry.setPrice(headOrder.getPrice());
			tradeEntry.setQuantity(qtyTraded);
			tradeEntry.setTradeTime(LocalDateTime.now());
			remoteServices.saveTrade(tradeEntry);
			remoteServices.updateOrders(tradeEntry);
		}
		logger.trace("Exiting processOrderList: " + incomingOrder + " Remaining Qty = " + qtyRemaining);
		execPosRecorder.recordExecutionPoint(className, "processOrderList", incomingOrder.getOrderId(), "exit");
		return qtyRemaining;
	}
	
	public synchronized void cancelOrder(long orderId) {
		Iterator<OrderBookItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			OrderBookItem o = iterator.next();
			if (o.getOrderId() == orderId) {
				iterator.remove();
			}
		}
	}
	
	
	public void modifyOrder(int qId, HashMap<String, String> quote) {
		// TODO implement modify order
		// Remember if price is changed must check for clearing.
	}
	
	
	public int getVolumeAtPrice(Side side, BigDecimal price) {
	    final BigDecimal clippedPrice = clipPrice(price);
		int volume = items.stream().filter(o -> o.getSide() == side && o.getPrice().equals(clippedPrice))
				.mapToInt(o -> o.getQuantity()).sum();
		return volume;
		
	}
	
	public BigDecimal getBestBid() {
		BigDecimal bestBid = BigDecimal.ZERO;
		Optional<OrderBookItem> bestBidOrder = items.stream()
				.filter(o -> o.getSide() == Side.BUY)
				.sorted(Comparator.comparing(OrderBookItem::getPrice).reversed()).findFirst();
		if (bestBidOrder.isPresent()) {
			bestBid = bestBidOrder.get().getPrice();
		}
		return bestBid;
	}
	
	public BigDecimal getWorstBid() {
		BigDecimal worstBid = BigDecimal.ZERO;
		Optional<OrderBookItem> worstBidOrder = items.stream()
				.filter(o -> o.getSide() == Side.BUY)
				.sorted(Comparator.comparing(OrderBookItem::getPrice)).findFirst();
		if (worstBidOrder.isPresent()) {
			worstBid = worstBidOrder.get().getPrice();
		}
		return worstBid;
	}
	
	public BigDecimal getBestOffer() {
		BigDecimal bestOffer = BigDecimal.ZERO;
		Optional<OrderBookItem> bestOfferOrder = items.stream()
				.filter(o -> o.getSide() == Side.SELL)
				.sorted(Comparator.comparing(OrderBookItem::getPrice)).findFirst();
		if (bestOfferOrder.isPresent()) {
			bestOffer = bestOfferOrder.get().getPrice();
		}
		return bestOffer;
	}
	
	public BigDecimal getWorstOffer() {
		BigDecimal worstOffer = BigDecimal.ZERO;
		Optional<OrderBookItem> worstOfferOrder = items.stream()
				.filter(o -> o.getSide() == Side.SELL)
				.sorted(Comparator.comparing(OrderBookItem::getPrice).reversed()).findFirst();
		if (worstOfferOrder.isPresent()) {
			worstOffer = worstOfferOrder.get().getPrice();
		}
		return worstOffer;
	}
	
	public int volumeOnSide(Side side) {
		if (side != Side.BUY && side != Side.SELL) {
			throw new IllegalArgumentException("order neither market nor limit: " + 
					side);
		}
		int volume = items.stream()
						.filter(o -> o.getSide() == side)
						.mapToInt(o -> o.getQuantity())
						.sum();
		return volume;
	}
	
	public BigDecimal getTickSize() {
		return tickSize;
	}
	
	public BigDecimal getSpread() {
		BigDecimal minOfferPrice = BigDecimal.ZERO;
		Optional<BigDecimal> minOfferPriceOpt = items.stream()
							  						.filter(o -> o.getSide() == Side.SELL)
							  						.min(Comparator.comparing(OrderBookItem::getPrice))
							  						.map(o -> o.getPrice());
		if (minOfferPriceOpt.isPresent()) {
			minOfferPrice = minOfferPriceOpt.get();
		}
			
		BigDecimal maxBidPrice = BigDecimal.ZERO;
		Optional<BigDecimal> maxBidPriceOpt = items.stream()
							  						.filter(o -> o.getSide() == Side.BUY)
							  						.min(Comparator.comparing(OrderBookItem::getPrice))
							  						.map(o -> o.getPrice());
		if (maxBidPriceOpt.isPresent()) {
			maxBidPrice = maxBidPriceOpt.get();
		}

		return minOfferPrice.subtract(maxBidPrice);
	}
	
	public synchronized BigDecimal getMid() {
		return this.getBestBid().add(this.getSpread().divide(TWO));
	}
	
	public boolean bidsAndAsksExist() {
		long bidCount = items.stream().filter(o -> o.getSide() == Side.BUY).count();
		long offerCount = items.stream().filter(o -> o.getSide() == Side.SELL).count();
		return bidCount > 0 && offerCount > 0;
	}
	
	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append("Time: " + formatter.format(LocalDateTime.now()) + "\n");
		message.append("-------- The Order Book --------\n");
		message.append("  ").append(this.productSymbol).append("\n");
		message.append("   ------- Bid  Book --------   \n");
		String bids = this.items.stream().filter(o -> o.getSide() == Side.BUY)
				.map(o -> o.toString())
				.reduce("", (a, b) -> a + "\n    " + b);
		message.append(bids + "\n");
		message.append("   ------- Offer  Book --------   \n");
		String offers = this.items.stream().filter(o -> o.getSide() == Side.SELL)
				.map(o -> o.toString())
				.reduce("", (a, b) -> a + "\n    " + b);
		message.append(offers + "\n");
		message.append("---------------------------------\n");
		return message.toString();
	}

	@Override
	public long getProductId() {
		return this.productId;
	}

	@Override
	public void setProductId(long productId) {
		this.productId = productId;
	}

	@Override
	public String getProductSymbol() {
		return this.productSymbol;
	}

	@Override
	public void setProductSymbol(String productSymbol) {
		this.productSymbol = productSymbol;
	}

	@Override
	public void setOrderBookItems(List<OrderBookItem> items) {
		this.items = items;
	}
	
}
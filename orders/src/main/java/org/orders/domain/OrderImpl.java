package org.atlas.engine.financialexchange.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderImpl implements Order {

	static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss.SSS");

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "product_id")
	private long productId;
	
	@Column(name = "participant_id")
	private long participantId;
	
	@Column(name = "entry_time")
	private LocalDateTime entryTime;
	
	@Column(name = "side")
	@Enumerated(EnumType.STRING)
	private Side side;
	
	@Column(name = "quantity")
	private int quantity;
	
	@Column(name = "traded_quantity")
	private int tradedQuantity;
	
	@Column(name = "booked_quantity")
	private int bookedQuantity;
	
	@Column(name = "cancelled_quantity")
	private int cancelledQuantity;
	
	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private OrderStatus status;
	
	@Column(name = "order_type")
	@Enumerated(EnumType.STRING)
	private OrderType type;
	
	@Column(name = "longevity")
	@Enumerated(EnumType.STRING)
	private OrderLongevity longevity;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public long getParticipantId() {
		return participantId;
	}

	public void setParticipantId(long participantId) {
		this.participantId = participantId;
	}

	public LocalDateTime getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(LocalDateTime entryTime) {
		this.entryTime = entryTime;
	}

	public int getTradedQuantity() {
		return tradedQuantity;
	}

	public Side getSide() {
		return this.side;
	}

	public void setSide(Side side) {
		this.side = side;
	}

	@Override
	public int getQuantity() {
		return this.quantity;
	}

	@Override
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public BigDecimal getPrice() {
		return this.price;
	}

	@Override
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public OrderStatus getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	@Override
	public OrderType getType() {
		return this.type;
	}

	@Override
	public void setType(OrderType type) {
		this.type = type;
	}

	@Override
	public OrderLongevity getLongevity() {
		return this.longevity;
	}

	@Override
	public void setLongevity(OrderLongevity longevity) {
		this.longevity = longevity;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return this.getId() == order.getId();
    }

    @Override
    public int hashCode() {
        if (getId() > Integer.MAX_VALUE) {
        	return (int) (getId() % Integer.MAX_VALUE);
        } else {
        	return Integer.parseInt(Long.toString(getId()));
        }
    }
    
	public String toString() {
		StringBuffer message = new StringBuffer();
		message.append(" id = ").append(this.getId()).append(";");
		message.append(" time = ").append(formatter.format(LocalDateTime.now())).append(";");
		message.append(" side = ").append(this.getSide()).append(";");
		message.append(" qty = ").append(this.getQuantity()).append(";");
		message.append(" price = ").append(this.getPrice()).append(";");
		message.append(" productId: ").append(this.getProductId());
		message.append(" participantId: ").append(this.getParticipantId());
		return message.toString();
	}

	public int getTradedQantity() {
		return tradedQuantity;
	}

	public void setTradedQuantity(int tradedQuantity) {
		this.tradedQuantity = tradedQuantity;
	}
	
	public int getBookedQuantity() {
		return bookedQuantity;
	}

	public void setBookedQuantity(int bookedQuantity) {
		this.bookedQuantity = bookedQuantity;
	}

	public int getCancelledQuantity() {
		return cancelledQuantity;
	}

	public void setCancelledQuantity(int cancelledQuantity) {
		this.cancelledQuantity = cancelledQuantity;
	}

}

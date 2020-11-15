package org.atlas.engine.financialexchange.orders.domain;

import java.time.LocalDateTime;

public interface Tradable {
	public Long getId();
	public void setId(Long id);
	public long getProductId();
	public void setProductId(long productId);
	public LocalDateTime getEntryTime();
	public void setEntryTime(LocalDateTime entryTime);
	public long getParticipantId();
	public void setParticipantId(long participantId);
	public Side getSide();
	public void setSide(Side side);
}


package org.atlas.engine.financialexchange.products.repository;

import java.util.Optional;

import org.atlas.engine.financialexchange.products.domain.Equity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquityRepository  extends JpaRepository<Equity, Long> {
	public Optional<Equity> findBySymbol(String symbol);
}

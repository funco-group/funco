package com.found_404.funco.trade.dto.response;

import lombok.Builder;

@Builder
public record CoinValuation (
	String ticker,
	Long price,
	Long valuation) {
}
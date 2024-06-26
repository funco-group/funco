package com.found_404.funco.trade.domain;

import static com.found_404.funco.global.util.DecimalCalculator.*;
import static com.found_404.funco.global.util.DecimalCalculator.ScaleType.*;

import org.hibernate.annotations.Comment;

import com.found_404.funco.global.entity.BaseEntity;
import com.found_404.funco.trade.domain.type.TradeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OpenTrade extends BaseEntity {

    @Comment("member seq")
    @Column(nullable = false)
    private Long memberId;

    @Comment("코인명")
    @Column(length = 20)
    private String ticker;

    @Comment("거래 구분")
    @Enumerated(value = EnumType.STRING)
    private TradeType tradeType;

    @Comment("수량")
    private Double volume;

    @Comment("주문 금액")
    private Long orderCash;

    @Comment("가격")
    private Double price;

    @Comment("구매한 가격")
    private Double buyPrice;

    @Builder
    public OpenTrade(Long memberId, String ticker, TradeType tradeType, Double volume, Long orderCash, Double price, Double buyPrice) {
        this.memberId = memberId;
        this.ticker = ticker;
        this.tradeType = tradeType;
        this.volume = volume;
        this.orderCash = orderCash;
        this.price = price;
        this.buyPrice = buyPrice;
    }

    public static Trade toTrade(OpenTrade openTrade, Double tradePrice) {
        return Trade.builder()
            .ticker(openTrade.getTicker())
            .volume(openTrade.getVolume())
            .price(tradePrice) // 시장가
            .orderCash((long)multiple(openTrade.volume, tradePrice, CASH_SCALE)) // 시장가로 체결
            .tradeType(openTrade.getTradeType())
            .memberId(openTrade.memberId)
            .build();
    }

}

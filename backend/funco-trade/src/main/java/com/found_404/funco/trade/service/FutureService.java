package com.found_404.funco.trade.service;

import com.found_404.funco.crypto.cryptoPrice.CryptoPrice;
import com.found_404.funco.feignClient.service.MemberService;
import com.found_404.funco.trade.domain.ActiveFuture;
import com.found_404.funco.trade.domain.repository.ActiveFutureRepository;
import com.found_404.funco.trade.domain.repository.FutureTradeRepository;
import com.found_404.funco.trade.domain.type.TradeType;
import com.found_404.funco.trade.dto.FutureTrade;
import com.found_404.funco.trade.dto.request.RequestBuyFutures;
import com.found_404.funco.trade.dto.ActiveFutureDto;
import com.found_404.funco.trade.exception.TradeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.found_404.funco.global.util.DecimalCalculator.ScaleType.CASH_SCALE;
import static com.found_404.funco.global.util.DecimalCalculator.ScaleType.NORMAL_SCALE;
import static com.found_404.funco.global.util.DecimalCalculator.divide;
import static com.found_404.funco.global.util.DecimalCalculator.multiple;
import static com.found_404.funco.trade.exception.TradeErrorCode.NOT_FOUND_TRADE;

@Service
@Slf4j
@RequiredArgsConstructor
public class FutureService {
    private final ActiveFutureRepository activeFutureRepository;
    private final FutureTradeRepository futureTradeRepository;
    private final CryptoPrice cryptoPrice;

    private final MemberService memberService;


    public void buyFuturesLong(Long memberId, RequestBuyFutures requestBuyFutures) {
        ActiveFuture activeFuture = activeFutureRepository.save(getActiveFuture(memberId, TradeType.LONG, requestBuyFutures));

        // 배율
        double rate = (int) divide(100, activeFuture.getLeverage(), NORMAL_SCALE);
        long liquidatedPrice = activeFuture.getPrice() - (long) multiple(activeFuture.getPrice(), rate, CASH_SCALE);

        log.info("[Long] member:{} {} 가격 {} 아래로 청산됩니다.", memberId, activeFuture.getTicker(), liquidatedPrice);
        cryptoPrice.addTrade(requestBuyFutures.ticker(), activeFuture.getId(), TradeType.LONG, liquidatedPrice);
    }

    public void buyFuturesShort(Long memberId, RequestBuyFutures requestBuyFutures) {
        ActiveFuture activeFuture = activeFutureRepository.save(getActiveFuture(memberId, TradeType.SHORT, requestBuyFutures));

        // 배율
        double rate = (int) divide(100, activeFuture.getLeverage(), NORMAL_SCALE);
        long liquidatedPrice = activeFuture.getPrice() + (long) multiple(activeFuture.getPrice(), rate, CASH_SCALE);
        log.info("[Short] member:{} {} 가격 {} 위로 청산됩니다.", memberId, activeFuture.getTicker(), liquidatedPrice);
        cryptoPrice.addTrade(requestBuyFutures.ticker(), activeFuture.getId(), TradeType.SHORT, liquidatedPrice);
    }

    private ActiveFuture getActiveFuture(Long memberId, TradeType tradeType, RequestBuyFutures requestBuyFutures) {
        return ActiveFuture.builder()
                .ticker(requestBuyFutures.ticker())
                .tradeType(tradeType)
                .orderCash(requestBuyFutures.orderCash())
                .memberId(memberId)
                .leverage(requestBuyFutures.leverage())
                .price(cryptoPrice.getTickerPrice(requestBuyFutures.ticker()))
                .build();
    }

    @Transactional
    public void settlement(Long memberId, Long futureId) {
        ActiveFuture activeFuture = activeFutureRepository.findById(futureId)
                .orElseThrow(() -> new TradeException(NOT_FOUND_TRADE));

        long price = cryptoPrice.getTickerPrice(activeFuture.getTicker());

        // 투입금 + ( 수익(+-) * 레버리지 )
        long result = (price - activeFuture.getPrice()) * activeFuture.getLeverage();
        long settlement = activeFuture.getOrderCash() + result;

        activeFutureRepository.delete(activeFuture);
        futureTradeRepository.save(com.found_404.funco.trade.domain.FutureTrade.fromActiveFutures(activeFuture, settlement));

        // 멤버 자산 증가
        memberService.updateMemberCash(memberId, settlement);
    }

    public ActiveFutureDto getActiveFutures(Long memberId, String ticker) {
        return ActiveFutureDto.fromEntity(activeFutureRepository.findByMemberIdAndTicker(memberId, ticker)
                .orElseThrow(()->new TradeException(NOT_FOUND_TRADE)));
    }

    public List<FutureTrade> getFutures(Long memberId, String ticker) {
        return futureTradeRepository.findAllByMemberIdAndTicker(memberId, ticker)
                .stream()
                .map(FutureTrade::fromEntity)
                .toList();
    }
}
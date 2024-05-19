
package com.found_404.funcomember.feignClient.service;

import com.found_404.funcomember.feignClient.client.AssetServiceClient;
import com.found_404.funcomember.feignClient.dto.AssetTradeType;
import com.found_404.funcomember.feignClient.dto.request.TotalAssetHistoryRequest;
import com.found_404.funcomember.member.exception.MemberException;
import com.found_404.funcomember.portfolio.domain.Subscribe;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.found_404.funcomember.member.exception.MemberErrorCode.OTHER_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssetService {
    private final AssetServiceClient assetServiceClient;
    private final String SERVER_NAME = "[asset-service]";

    @Async
    public void createAssetHistory(Subscribe subscribe, AssetTradeType assetTradeType, Long cash) {
        try {
            this.assetServiceClient.createCoinHistory(TotalAssetHistoryRequest.builder()
                    .assetTradeType(assetTradeType)
                    .price(Double.valueOf(subscribe.getOrderCash()) *
                            (assetTradeType.equals(AssetTradeType.SELL_PORTFOLIO) ? 1 : -1))
                    .memberId(assetTradeType.equals(AssetTradeType.SELL_PORTFOLIO) ? subscribe.getToMember().getId() : subscribe.getFromMember().getId())
                    .portfolioName(assetTradeType.equals(AssetTradeType.SELL_PORTFOLIO) ? subscribe.getToMember().getNickname() : subscribe.getFromMember().getNickname())
                    .orderCash(subscribe.getOrderCash())
                    .endingCash(cash)
                    .build());

        } catch (FeignException e) {
            log.error("{} createAssetHistory error : {}", SERVER_NAME, e.getMessage());
            throw new MemberException(OTHER_SERVER_ERROR);
        }
    }

}
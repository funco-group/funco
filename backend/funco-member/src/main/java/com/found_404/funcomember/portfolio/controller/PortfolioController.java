package com.found_404.funcomember.portfolio.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.found_404.funcomember.global.memberIdHeader.AuthMemberId;
import com.found_404.funcomember.portfolio.dto.request.PortfolioStatusRequest;
import com.found_404.funcomember.portfolio.dto.request.SubscribeRequest;
import com.found_404.funcomember.portfolio.service.PortfolioService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/v1/members/portfolio")
@RequiredArgsConstructor
@RestController
public class PortfolioController {
	private final PortfolioService portfolioService;

	@PatchMapping
	public ResponseEntity<Void> modifyPortfolioStatus(@AuthMemberId Long memberId,
		@RequestBody @Valid PortfolioStatusRequest portfolioStatusRequest
	) {
		portfolioService.updatePortfolioStatus(memberId, portfolioStatusRequest);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PostMapping("/subscribe")
	public ResponseEntity<Void> addPortfolio(
			@AuthMemberId Long memberId,
			@RequestBody @Valid SubscribeRequest subscribeRequest) {
		portfolioService.createPortfolio(memberId, subscribeRequest);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}

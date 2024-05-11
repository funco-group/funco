package com.found_404.funcomember.auth.service;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.found_404.funcomember.auth.client.OauthMemberClientComposite;
import com.found_404.funcomember.auth.dto.OauthDto;
import com.found_404.funcomember.auth.dto.response.LoginResponse;
import com.found_404.funcomember.auth.dto.response.TokenResponse;
import com.found_404.funcomember.auth.type.OauthServerType;
import com.found_404.funcomember.global.security.service.TokenService;
import com.found_404.funcomember.member.domain.Member;
import com.found_404.funcomember.member.domain.repository.MemberRepository;
import com.found_404.funcomember.member.domain.type.MemberStatus;
import com.found_404.funcomember.member.domain.type.PortfolioStatusType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthService {

	private final TokenService tokenService;

	private final MemberRepository memberRepository;
	private final OauthMemberClientComposite oauthMemberClientComposite;
	private final RedisTemplate<String, Object> tokenRedisTemplate;
	// private final NotificationService notificationService;

	private final long INIT_CASH = 10_000_000;

	// 가입되어 있지 않다면 저장(회원가입)
	public LoginResponse login(HttpServletResponse response, OauthServerType oauthServerType, String authCode) {
		OauthDto dto = oauthMemberClientComposite.fetch(oauthServerType, authCode);
		Member member = memberRepository.findByOauthId(dto.member().getOauthId())
			.orElseGet(() -> Member
				.builder()
				.nickname(dto.member().getNickname())
				.profileUrl(dto.member().getProfileUrl())
				.cash(INIT_CASH)
				.status(MemberStatus.NORMAL)
				.portfolioStatus(PortfolioStatusType.PUBLIC)
				.build());
		if (member.getOauthId() == null) {
			member.updateOauthId(dto.member().getOauthId());
			memberRepository.save(member);
		}

		// redis oauthAccessToken 저장
		// 추후 로그아웃 구현 시 필요할 수 있음
		HashOperations<String, Object, Object> hashOperations = tokenRedisTemplate.opsForHash();
		hashOperations.put(dto.member().getOauthId().getOauthServerId(), "oauthAccessToken", dto.accessToken());

		String refreshToken = tokenService.createRefreshToken(member);
		// 기존의 쿠키 설정을 문자열로 변환
		String cookieValue = "refreshToken=" + refreshToken +
			"; HttpOnly; Secure; Path=/; SameSite=None";

		// 응답 헤더에 쿠키 추가
		response.addHeader("Set-Cookie", cookieValue);

		return LoginResponse.builder()
			.profileUrl(member.getProfileUrl())
			.nickname(member.getNickname())
			.memberId(member.getId())
			.accessToken(tokenService.createToken(member))
			// .unReadCount(notificationService.getUnReadCount(member.getId()))
			.build();
	}

	public TokenResponse reissueToken(HttpServletRequest request, HttpServletResponse response) {

		// 유효하다면 accessToken 재발급
		// 기존 코드 Refactoring 필요
		return tokenService.reissueAccessToken(request, response);
	}

}

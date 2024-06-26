package com.found_404.funco.global.token.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenErrorCode {
	MEMBER_NOT_FOUND("해당 회원을 찾을 수 없습니다."),
	EXPIRED_TOKEN("만료된 토큰입니다."),
	EXPIRED_REFRESH_TOKEN("만료된 refresh 토큰입니다."),
	INVALID_TOKEN("유효하지 않은 토큰입니다."),
	EMPTY_TOKEN("토큰이 존재하지 않습니다."),
	AUTHORIZATION_KEY_DOES_NOT_EXIST("Authorization key가 존재하지 않습니다."),
	ERROR_CODE_500("서버 에러. 문의가 필요합니다.");

	private final String message;
}

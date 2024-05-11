package com.found_404.funcomember.auth.authCode;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.found_404.funcomember.auth.type.OauthServerType;

@Component
public class AuthCodeRequestUrlProviderComposite {

	private final Map<OauthServerType, AuthCodeRequestUrlProvider> mapping;

	public AuthCodeRequestUrlProviderComposite(Set<AuthCodeRequestUrlProvider> providers) {
		mapping = providers.stream()
			.collect(toMap(
				AuthCodeRequestUrlProvider::supportServer,
				identity()
			));
	}

	public String provide(OauthServerType oauthServerType) {
		return getProvider(oauthServerType).provide();
	}

	private AuthCodeRequestUrlProvider getProvider(OauthServerType oauthServerType) {
		return Optional.ofNullable(mapping.get(oauthServerType))
			.orElseThrow(() -> new RuntimeException("지원하지 않는 소셜 로그인 타입입니다."));
	}
}

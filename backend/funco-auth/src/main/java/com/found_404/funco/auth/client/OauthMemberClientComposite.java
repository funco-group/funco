package com.found_404.funco.auth.client;

import static java.util.function.UnaryOperator.*;
import static java.util.stream.Collectors.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.found_404.funco.auth.dto.OauthDto;
import com.found_404.funco.auth.type.OauthServerType;

@Component
public class OauthMemberClientComposite {
	private final Map<OauthServerType, OauthMemberClient> mapping;

	public OauthMemberClientComposite(Set<OauthMemberClient> clients) {
		mapping = clients.stream()
			.collect(toMap(
				OauthMemberClient::supportServer,
				identity()
			));
	}

	public OauthDto fetch(OauthServerType oauthServerType, String authCode) {
		return getClient(oauthServerType).fetch(authCode);
	}

	private OauthMemberClient getClient(OauthServerType oauthServerType) {
		return Optional.ofNullable(mapping.get(oauthServerType))
			.orElseThrow(() -> new RuntimeException("지원하지 않는 소셜 로그인 타입입니다."));
	}
}
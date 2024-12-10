/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.example.magiclink;

import java.net.URI;

import reactor.core.publisher.Mono;

import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.authentication.ott.ServerOneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MagicLinkOneTimeTokenGenerationSuccessHandler implements ServerOneTimeTokenGenerationSuccessHandler {

	private final MailSender mailSender;

	private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	public MagicLinkOneTimeTokenGenerationSuccessHandler(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, OneTimeToken oneTimeToken) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(exchange.getRequest().getURI())
			.replacePath(null)
			.replaceQuery(null)
			.fragment(null)
			.path("/login/ott")
			.queryParam("token", oneTimeToken.getTokenValue());
		String magicLink = builder.toUriString();
		builder.replacePath(null).replaceQuery(null).path("/ott/sent");
		String redirectLink = builder.toUriString();
		return this.mailSender
			.send("johndoe@example.com", "Your Spring Security One Time Token",
					"Use the following link to sign in into the application: " + magicLink)
			.then(this.redirectStrategy.sendRedirect(exchange, URI.create(redirectLink)));
	}

}

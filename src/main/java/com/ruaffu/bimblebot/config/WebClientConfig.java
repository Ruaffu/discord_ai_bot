package com.ruaffu.bimblebot.config;

import org.apache.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.*;

@Configuration
public class WebClientConfig {
	@Value("${openai.api.key}")
	private String openAiApiKey;

	@Value("${discordbot.url}")
	private String url;

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
				.baseUrl(url)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.build();
	}
}

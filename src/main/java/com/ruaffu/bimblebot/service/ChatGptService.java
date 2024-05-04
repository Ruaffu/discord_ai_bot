package com.ruaffu.bimblebot.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.*;

import com.fasterxml.jackson.databind.*;

import reactor.core.publisher.Mono;

@Service
public class ChatGptService {
	private final WebClient webClient;

	@Autowired
	public ChatGptService(WebClient webClient) {
		this.webClient = webClient;
	}

	public Mono<String> sendMessageToChatGPT(String message) {
		return webClient.post()
				.uri("/v1/chat/completions")
				.bodyValue(buildRequestBody(message))
				.retrieve()
				.bodyToMono(JsonNode.class)
				.map(jsonNode -> jsonNode.get("choices").get(0).get("message").asText());
	}

	private Object buildRequestBody(String message) {
		Map<String, Object> data = new HashMap<>();
		data.put("model", "gpt-3.5-turbo");  // Specify the model, adjust if necessary
		data.put("prompt", message);
		data.put("max_tokens", 150);  // Set max tokens as needed

		return data;
	}
}

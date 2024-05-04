package com.ruaffu.bimblebot.service;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.reactive.function.client.*;

import com.google.gson.*;

import lombok.extern.slf4j.*;

import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ChatGptService {
	private final WebClient webClient;

	@Autowired
	public ChatGptService(WebClient webClient) {
		this.webClient = webClient;
	}

	public Mono<String> sendMessageToChatGPT(String message) {
		return webClient.post()
				.bodyValue(buildRequestBody(message))
				.retrieve()
				.bodyToMono(String.class)
				.map(responseBody -> {
					Gson gson = new Gson();
					JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

					// Extract the "content" value
					String content = responseJson.getAsJsonArray("choices")
							.get(0).getAsJsonObject()
							.get("message").getAsJsonObject()
							.get("content").getAsString();

					log.debug("response : {}", content);

					return content;
				}).onErrorReturn("An error occurred while generating the response.");
	}

	private Object buildRequestBody(String message) {
		List<Map<String, String>> messages = new ArrayList<>();
		Map<String, String> userMessage = new HashMap<>();
		userMessage.put("role", "user");
		userMessage.put("content", message);
		messages.add(userMessage);

		Map<String, Object> data = new HashMap<>();
		data.put("model", "gpt-4-turbo");  // Specify the model, adjust if necessary
		data.put("messages", messages);

		return data;
	}
}

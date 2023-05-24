package com.ruaffu.bimblebot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DiscordBot extends ListenerAdapter {
    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${discordbot.url}")
    private String url;

    private WebClient webClient;

    @PostConstruct
    public void init(){
        webClient = WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    public void startBot() throws Exception {
        JDABuilder.createDefault(botToken)
                .addEventListeners(this)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        log.info("User : {} asked a question",event.getAuthor().getName());

        String message = event.getMessage().getContentRaw();

        log.info("message: {}",message);
        if (message.contains(event.getJDA().getSelfUser().getAsMention())) {
            String messageContent = event.getMessage().getContentRaw();

            // Define the regex pattern for matching user/bot mentions
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");

            // Create a matcher object for the message content
            Matcher mentionMatcher = mentionPattern.matcher(messageContent);

            // Remove all user/bot mentions from the message content
            String trimmedMessage = mentionMatcher.replaceAll("");

            // Trim leading and trailing whitespace
            trimmedMessage = trimmedMessage.trim();

            generateChatGPTResponse(trimmedMessage)
                    .subscribe(resp -> {
                                // Send the response back to the Discord channel
                                event.getChannel().sendMessage(resp).queue();
                            },
                            error -> {
                                log.error("An error occurred while generating the response.", error);
                                event.getChannel().sendMessage("An error occurred while generating the response.").queue();
                            });
        }

    }

    private Mono<String> generateChatGPTResponse(String message) {
            log.info("Question: {}",message);
            String requestBody = String.format("{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"user\", \"content\": \" %s\"}]}", message);
            return webClient.post()
                    .body(BodyInserters.fromValue(requestBody))
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

                        log.info("response : {}",content);

                        return content;
                    }).onErrorReturn("An error occurred while generating the response.");

    }
}

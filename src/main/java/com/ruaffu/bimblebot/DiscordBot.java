package com.ruaffu.bimblebot;

import java.util.regex.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import com.ruaffu.bimblebot.service.*;

import lombok.extern.slf4j.*;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Slf4j
@Component
public class DiscordBot extends ListenerAdapter {
	@Value("${discord.bot.token}")
	private String botToken;
	private final ChatGptService chatGptService;

	@Autowired
	public DiscordBot(ChatGptService chatGptService) {
		this.chatGptService = chatGptService;
	}

	public void startBot() throws Exception {
		JDABuilder.createDefault(botToken)
				.addEventListeners(this)
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.build();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		if (event.getAuthor().isBot())
			return;

		log.debug("User : {} asked a question", event.getAuthor().getName());

		String message = event.getMessage().getContentRaw();

		log.debug("message: {}", message);
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

			chatGptService.sendMessageToChatGPT(trimmedMessage)
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

}

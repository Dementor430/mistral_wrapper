package sh.ashral.mistralWrapper.commands;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import sh.ashral.mistralWrapper.MistralWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChatCommand implements BasicCommand {

    private final String systemPrompt =
            "You are a Minecraft assistant helping players. " +
                    "Use Minecraft color formatting in your responses. " +
                    "Here are the available color codes:\n" +
                    "&0: Black, &1: Dark Blue, &2: Dark Green, &3: Dark Aqua, " +
                    "&4: Dark Red, &5: Dark Purple, &6: Gold, &7: Gray, " +
                    "&8: Dark Gray, &9: Blue, &a: Green, &b: Aqua, " +
                    "&c: Red, &d: Pink, &e: Yellow, &f: White\n" +
                    "Formatting codes: &l: Bold, &o: Italic, &n: Underlined, &m: Strikethrough, &r: Reset\n" +
                    "Example: '&aHello &bWorld!' will display 'Hello' in green and 'World!' in blue.\n" +
                    "Use these formatting options to highlight important information and make your responses more appealing.";

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        if (!(commandSourceStack.getSender() instanceof Player)) {
            commandSourceStack.getSender().sendMessage(ChatColor.RED + "This command can only be used by players.");
            return;
        }

        Player player = (Player) commandSourceStack.getSender();
        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /llm <message>");
            return;
        }

        String message = String.join(" ", args);
        player.sendMessage(ChatColor.GRAY + "Thinking...");

        CompletableFuture.supplyAsync(() -> {
            OpenAiChatModel openAiChatModel = MistralWrapper.getInstance().openAiChatModel;

            // Create a list of chat messages with system prompt
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.add(new UserMessage(message));

            // Send the messages to the model
            return openAiChatModel.chat(messages);
        }).thenAccept(response -> {
            MistralWrapper.getInstance().getServer().getScheduler().runTask(
                    MistralWrapper.getInstance(),
                    () -> player.sendMessage(ChatColor.GREEN + "LLM: " +
                            ChatColor.translateAlternateColorCodes('&', response.aiMessage().text()))
            );
        });
    }
}

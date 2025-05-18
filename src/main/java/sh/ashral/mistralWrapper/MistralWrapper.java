package sh.ashral.mistralWrapper;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bukkit.plugin.java.JavaPlugin;
import sh.ashral.mistralWrapper.commands.ChatCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class MistralWrapper extends JavaPlugin {
    private static MistralWrapper instance;
    public OpenAiChatModel openAiChatModel;
    public Map<String, String> chatSessions = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        if (!getConfig().contains("openai_provider")) {
            getConfig().set("openai_provider", "https://api.mistral.ai/v1");
        }
        if (!getConfig().contains("openai_key")) {
            getConfig().set("openai_key", "YOUR_MISTRAL_KEY");
        }
        if (!getConfig().contains("openai_model")) {
            getConfig().set("openai_model", "mistral-large-latest");
        }
        saveConfig();
        CompletableFuture.runAsync(this::initOpenAIModel);
        registerCommand("llm", new ChatCommand());

        getLogger().info("MistralWrapper wurde aktiviert!");
    }

    @Override
    public void onDisable() {
        chatSessions.clear();
        instance = null;
        getLogger().info("MistralWrapper wurde deaktiviert!");
    }

    public static MistralWrapper getInstance() {
        return instance;
    }

    public void initOpenAIModel() {
        String openAiKey = this.getConfig().getString("openai_key");
        String openAiProvider = this.getConfig().getString("openai_provider");
        String openAiModel = this.getConfig().getString("openai_model");

        // Validiere Konfigurationswerte
        if (openAiKey == null || openAiKey.isEmpty() || openAiKey.equals("YOUR_MISTRAL_KEY")) {
            this.getLogger().severe("API-Schlüssel nicht konfiguriert. Bitte konfiguriere deinen Mistral API-Schlüssel in der config.yml.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (openAiProvider == null || openAiProvider.isEmpty()) {
            this.getLogger().severe("API-Provider URL nicht konfiguriert. Bitte überprüfe deine config.yml.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (openAiModel == null || openAiModel.isEmpty()) {
            this.getLogger().severe("Modellname nicht konfiguriert. Bitte überprüfe deine config.yml.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            // Initialisiere das OpenAI-Chat-Modell
            this.openAiChatModel = OpenAiChatModel.builder()
                    .apiKey(openAiKey)
                    .modelName(openAiModel)
                    .baseUrl(openAiProvider)
                    .timeout(java.time.Duration.ofSeconds(60))
                    .build();
            this.getLogger().info("OpenAI-Chat-Modell erfolgreich initialisiert mit Modell: " + openAiModel);
        } catch (Exception e) {
            this.getLogger().severe("Fehler bei der Initialisierung des OpenAI-Chat-Modells: " + e.getMessage());
            this.getServer().getScheduler().runTask(this, () ->
                    this.getServer().getPluginManager().disablePlugin(this));
        }
    }
}

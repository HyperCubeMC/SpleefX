package io.github.spleefx.command.parent.sub;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.spleefx.SpleefX;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.command.sub.PluginSubcommand;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.chat.ChatComponent;
import io.github.spleefx.compatibility.chat.ChatEvents.ClickEvent;
import io.github.spleefx.compatibility.chat.ChatEvents.HoverEvent;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.PlaceholderUtil;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.plugin.Protocol;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.moltenjson.json.JsonBuilder;
import org.moltenjson.utils.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DebugSubcommand extends PluginSubcommand {

    private static final String HASTEBIN_ENDPOINT = "https://hastebin.com/documents";
    private static final Permission PERMISSION = new Permission("spleefx.admin.debug");
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor();

    public DebugSubcommand() {
        super("debug", c -> PERMISSION, "Get a full, verbose debug report for the plugin", c -> "/spleefx debug");
    }

    /**
     * Handles the command logic
     *
     * @param command The bukkit command
     * @param sender  Sender of the command
     * @param args    Command arguments
     * @return {@code false} if it is desired to send the help menu, true if otherwise.
     */
    @Override public boolean handle(Command command, CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("disablespectatingwarningmessage")) {
            SpleefX.getSpectatorSettings().setSendWarningOnStart(false);
            Chat.plugin(sender, "&aWarning message disabled.");
            return true;
        }
        Chat.plugin(sender, "&eCreating a full dump report. Please wait.");
        CompletableFuture<String> pasteURL = new CompletableFuture<>();
        AtomicLong elapsedMillis = new AtomicLong();
        POOL.submit(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            JsonBuilder debug = new JsonBuilder();

            // map server info
            JsonObject serverInfo = new JsonBuilder()
                    .map("Version", Protocol.VERSION + " (" + Bukkit.getBukkitVersion() + ") - [" + Bukkit.getVersion() + "]")
                    .map("Platform", Bukkit.getName())
                    .map("SpleefX Version", SpleefX.getPlugin().getDescription().getVersion())
                    .map("Java Version", System.getProperty("java.version")).buildJsonObject();

            // map plugins list
            JsonArray plugins = new JsonArray();
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                PluginDescriptionFile pdf = plugin.getDescription();
                JsonBuilder b = new JsonBuilder()
                        .map("Name", pdf.getName())
                        .map("Version", pdf.getVersion())
                        .map("Authors", pdf.getAuthors())
                        .map("Main", pdf.getMain())
                        .map("API Version", pdf.getAPIVersion());
                plugins.add(b.buildJsonObject());
            }

            // map plugin configuration
            JsonBuilder config = new JsonBuilder();
            for (Entry<String, Object> option : SpleefX.getPlugin().getConfig().getValues(true).entrySet()) {
                if (option.getValue() instanceof MemorySection) continue;
                config.map(option.getKey(), option.getValue());
            }

            JsonObject extensions = new JsonObject();

            // map extensions
            for (GameExtension extension : ExtensionsManager.EXTENSIONS.values()) {
                extensions.add(extension.getKey(), ArenaData.GSON.toJsonTree(extension));
            }

            // spectator settings
            JsonElement spectator = ArenaData.GSON.toJsonTree(SpleefX.getSpectatorSettings());

            debug.map("Server Info", serverInfo)
                    .map("Config", config.buildJsonObject())
                    .map("Extensions", extensions)
                    .map("Spectator settings", spectator)
                    .map("Plugin list", plugins);
            // create paste
            try {
                URL url = new URL(HASTEBIN_ENDPOINT);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-Agent", "SpleefX");
                con.setRequestMethod("POST");
                String text = debug.buildPretty();
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = text.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                String responseText;
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                    responseText = br.lines().map(line -> line + "\n").collect(Collectors.joining());
                    br.close();
                    String paste = "https://hastebin.com/raw/" + JsonUtils.getObjectFromString(responseText).get("key").getAsString();
                    elapsedMillis.set(stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    pasteURL.complete(paste);
                } else {
                    Exception e = new IllegalStateException(con.getResponseMessage());
                    e.printStackTrace();
                    pasteURL.completeExceptionally(e);
                    elapsedMillis.set(stopwatch.elapsed(TimeUnit.MILLISECONDS));
                }
            } catch (Throwable e) {
                e.printStackTrace();
                pasteURL.completeExceptionally(e);
                elapsedMillis.set(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        });
        pasteURL.thenAccept((url) -> {
            Chat.plugin(sender, "&aPaste created in &b" + PlaceholderUtil.NUMBER_FORMAT.format(elapsedMillis.get()) + "ms&a: &d" + url);
            if (sender instanceof Player) {
                ComponentJSON json = new ComponentJSON();
                ChatComponent c = new ChatComponent().setText("&a&lClick to add to your chat box".replace(" ", " &a&l"), true)
                        .setHoverAction(HoverEvent.SHOW_TEXT, "Click to add")
                        .setClickAction(ClickEvent.SUGGEST_COMMAND, url);
                json.append(c);
                CompatibilityHandler.getProtocol().send(json, sender);
            }
        });
        return true;
    }
}
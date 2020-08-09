package io.github.spleefx;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import io.github.spleefx.arena.ArenaManager;
import io.github.spleefx.arena.api.ArenaData;
import io.github.spleefx.arena.api.GameArena;
import io.github.spleefx.arena.bow.BowSpleefListener;
import io.github.spleefx.arena.spleef.SpleefListener;
import io.github.spleefx.arena.splegg.SpleggListener;
import io.github.spleefx.command.parent.*;
import io.github.spleefx.command.plugin.PluginCommandBuilder;
import io.github.spleefx.command.sub.base.StatsCommand.MenuListener;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.anticheat.SpartanHook;
import io.github.spleefx.compatibility.worldedit.SchematicManager;
import io.github.spleefx.converter.*;
import io.github.spleefx.data.DataProvider;
import io.github.spleefx.data.DataProvider.StorageType;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.StatisticsConfig;
import io.github.spleefx.data.papi.OldExpansionRemover;
import io.github.spleefx.data.papi.SpleefXPAPI;
import io.github.spleefx.economy.booster.ActiveBoosterLoader;
import io.github.spleefx.economy.booster.BoosterConsumer;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ability.DoubleJumpHandler;
import io.github.spleefx.extension.ability.GameAbility;
import io.github.spleefx.extension.ability.TripleArrowsAbility;
import io.github.spleefx.gui.JoinGUI.MenuSettings;
import io.github.spleefx.listeners.ArenaListener;
import io.github.spleefx.listeners.ConnectionListener;
import io.github.spleefx.listeners.RenameListener;
import io.github.spleefx.listeners.SignListener;
import io.github.spleefx.metrics.Metrics;
import io.github.spleefx.metrics.Metrics.AdvancedPie;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.perk.PerkShop;
import io.github.spleefx.scoreboard.ScoreboardListener;
import io.github.spleefx.scoreboard.sidebar.ScoreboardTicker;
import io.github.spleefx.spectate.*;
import io.github.spleefx.spectate.SpectatingListener.PickupListener;
import io.github.spleefx.util.io.CopyStore;
import io.github.spleefx.util.io.FileManager;
import io.github.spleefx.util.menu.GameMenu;
import io.github.spleefx.util.message.message.MessageImporter;
import io.github.spleefx.util.message.message.MessageManager;
import io.github.spleefx.util.plugin.DelayExecutor;
import io.github.spleefx.util.plugin.PluginSettings;
import io.github.spleefx.util.plugin.Protocol;
import io.github.spleefx.vault.VaultHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import me.clip.placeholderapi.PlaceholderAPI;
import me.lucko.helper.maven.LibraryLoader;
import me.lucko.helper.maven.LibraryLoader.Dependency;
import me.lucko.helper.maven.MavenLibrary;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.moltenjson.adapter.GameArenaAdapter;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.configuration.pack.ConfigurationPack;
import org.moltenjson.configuration.pack.DeriveFrom;
import org.moltenjson.configuration.select.SelectableConfiguration;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.configuration.tree.TreeConfigurationBuilder;
import org.moltenjson.json.JsonFile;
import org.moltenjson.utils.AdapterBuilder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static io.github.spleefx.extension.ExtensionsManager.EXTENSIONS_FOLDER;
import static io.github.spleefx.perk.GamePerk.PERKS;
import static io.github.spleefx.perk.GamePerk.PERKS_FOLDER;
import static java.io.File.separator;
import static org.moltenjson.configuration.tree.strategy.TreeNamingStrategy.STRING_STRATEGY;

@Getter
@MavenLibrary(groupId = "com.google.code.gson", artifactId = "gson", version = "2.8.5")
public final class SpleefX extends JavaPlugin implements Listener {

    public static final Map<String, Integer> BSTATS_EXTENSIONS = new ConcurrentHashMap<>();

    /**
     * The extensions tree
     */
    private TreeConfiguration<String, GameExtension> extensions;

    /**
     * The extensions tree
     */
    private TreeConfiguration<String, GamePerk> perks;

    /**
     * Main plugin instance
     */
    @Getter
    private static SpleefX plugin;

    private WorldEditPlugin worldEdit;
    private DelayExecutor<GameAbility> abilityDelays = new DelayExecutor<>(this);
    private CompatibilityHandler compatibilityHandler;
    private ArenaManager arenaManager;
    private BoosterConsumer boosterConsumer = new BoosterConsumer();
    private VaultHandler vaultHandler;
    private final FileManager<SpleefX> fileManager = new FileManager<>(this);
    private Logger pluginLogger;
    private SelectableConfiguration arenasConfig;
    private SelectableConfiguration statsFile = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("gui" + separator + "statistics-gui.json")), false, AdapterBuilder.GSON);
    private final SelectableConfiguration boostersFile = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("boosters" + separator + "boosters.json")), false, AdapterBuilder.GSON);
    private final SelectableConfiguration joinGuiFile = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("gui" + separator + "join-gui.json")), false, AdapterBuilder.GSON);

    private final SpectatingHandler spectatingHandler = new SpectatingHandler();
    private final ConfigurationPack<SpleefX> configurationPack = new ConfigurationPack<>(this, getDataFolder(), ArenaData.GSON);

    @Getter
    @DeriveFrom("boosters/active-boosters.json")
    private static ActiveBoosterLoader activeBoosterLoader = new ActiveBoosterLoader();

    @Getter
    @DeriveFrom("perks/-perks-shop.json")
    private static PerkShop perkShop = new PerkShop("&ePerk Shop", 3, new HashMap<>());

    @Getter
    @DeriveFrom("spectator-settings.json")
    private static SpectatorSettings spectatorMenu = new SpectatorSettings();

    private ScoreboardTicker scoreboardTicker;
    private final File arenasFolder = new File(getDataFolder(), "arenas");
    private ExtensionsManager extensionsManager;
    private MessageManager messageManager;

    private DataProvider dataProvider;

    public void loadMissing() {
        downloadIfMissing("ProtocolLib", "https://github.com/dmulloy2/ProtocolLib/releases/download/4.5.1/ProtocolLib.jar");
        downloadIfMissing("helper", "https://ci.lucko.me/job/helper/lastSuccessfulBuild/artifact/helper/target/helper.jar");
    }

    @SneakyThrows
    private void downloadIfMissing(String plugin, String url) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
            getLogger().info(StringUtils.capitalize(plugin) + " plugin not found. Downloading...");
            File pluginJAR = new File(getDataFolder(), ".." + separator + plugin + ".jar");

            URL website = new URL(url);
            URLConnection connection = website.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            connection.connect();

            ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(pluginJAR);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            Bukkit.getPluginManager().loadPlugin(pluginJAR);
        }
    }

    @Override
    public void onEnable() {
        loadMissing();
        if (CompatibilityHandler.shouldDisable()) {
            SpleefX.logger().severe("Unsupported server protocol: 1." + Protocol.EXACT);
            SpleefX.logger().severe("Please use 1.16.x or later for the plugin to function!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (CompatibilityHandler.missingWorldEdit()) {
            String v = "latest";
            String d = "https://dev.bukkit.org/projects/worldedit/files/latest";
            SpleefX.logger().severe("No WorldEdit found. Please download WorldEdit (" + v + "), from " + d);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            getLogger().info("Detected server version: " + Protocol.VERSION);

            messageManager = new MessageManager(this);
            messageManager.load(false);

            new MessageImporter(this);

            compatibilityHandler = new CompatibilityHandler();
            arenaManager = new ArenaManager(this);

            PluginSettings.load();
            fileManager.createDirectory(PluginSettings.STATISTICS_DIRECTORY.get());
            arenasFolder.mkdirs();
            statsFile.register(StatisticsConfig.class).associate();
            boostersFile.register(BoosterFactory.class).associate();
            try {
                configurationPack.register();
            } catch (IOException e) {
                e.printStackTrace();
            }
            joinGuiFile.register(MenuSettings.class).associate();

            List<Class<? extends GameArena>> arenaClasses = new ArrayList<>();

            // <-- Replace with the id of your plugin!
            FileConfiguration arenaTypes = YamlConfiguration.loadConfiguration(fileManager.createFile("arenas" + separator + "arena-types.yml"));

            for (String className : arenaTypes.getStringList("ArenaSubTypes")) {
                try {
                    Class<?> c = Class.forName(className);
                    if (GameArena.class.isAssignableFrom(c)) {
                        arenaClasses.add((Class<? extends GameArena>) c);
                    } else {
                        logger().warning("Failed to register arena type: Class does not extend GameArena: " + className);
                    }
                } catch (ClassNotFoundException e) {
                    logger().warning("Failed to register arena type: Class not found: " + className);
                }
            }

            arenaClasses.forEach(GameArenaAdapter::registerArena);
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            getServer().getPluginManager().registerEvents(this, this);

            extensions = new TreeConfigurationBuilder<String, GameExtension>(EXTENSIONS_FOLDER, STRING_STRATEGY)
                    .setExclusionPrefixes(ImmutableList.of("-"))
                    .setDataMap(new HashMap<>())
                    .searchSubdirectories()
                    .setGson(ArenaData.GSON)
                    .setLazy(false)
                    .setRestrictedExtensions(ImmutableList.of("json"))
                    .build();
            perks = new TreeConfigurationBuilder<String, GamePerk>(PERKS_FOLDER, STRING_STRATEGY)
                    .setExclusionPrefixes(ImmutableList.of("-"))
                    .setDataMap(new HashMap<>())
                    .searchSubdirectories()
                    .setGson(ArenaData.GSON)
                    .setLazy(false)
                    .setRestrictedExtensions(ImmutableList.of("json"))
                    .build();
            Map<String, GameExtension> data = extensions.load(GameExtension.class);
            try {
                PERKS.putAll(perks.load(GamePerk.class));
                configurationPack.updateField("perkShop");
            } catch (IOException e) {
                e.printStackTrace();
            }
            extensionsManager = new ExtensionsManager(this);
            arenasConfig = SelectableConfiguration.of(JsonFile.of(arenasFolder, "arenas.json"), false, ArenaData.GSON)
                    .register(GameArena.class).associate();

            int original = GameArena.ARENAS.get().size();
            GameArena.ARENAS.get().values().removeIf(Objects::isNull); // Filter out arenas which couldn't be loaded
            int modified = GameArena.ARENAS.get().size();
            logger().info("Successfully loaded " + modified + " arena" + (modified == 1 ? "" : "s") + " out of " + original);

            PluginManager p = Bukkit.getPluginManager();

            p.registerEvents(new TripleArrowsAbility(abilityDelays), this);

            p.registerEvents(new SignListener(this), this);
            p.registerEvents(new ConnectionListener(), this);
            p.registerEvents(new RenameListener(), this);
            p.registerEvents(new ArenaListener(), this);
            p.registerEvents(new CopyStore(), this);
            p.registerEvents(new BowSpleefListener(this), this);
            p.registerEvents(new SpleefListener(), this);
            p.registerEvents(new SpleggListener(this), this);
            p.registerEvents(new MenuListener(), this);
            p.registerEvents(new SpectatingListener(), this);
            p.registerEvents(new JoinWarningListener(), this);
            p.registerEvents(new PickupListener(), this);

            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
                p.registerEvents(new ArenaListener.WGListener(this), this);
            else
                p.registerEvents(new ArenaListener.BlockBreakListener(this), this);

            Preconditions.checkNotNull(getCommand("spleefx")).setExecutor(new CommandSpleefX());
            Preconditions.checkNotNull(getCommand("sploofx")).setExecutor(new CommandSpleefX());
            CommandExecutor def = new CustomExtensionCommand();

            data.forEach((key, extension) -> extension.getExtensionCommands().forEach(command -> new PluginCommandBuilder(command, SpleefX.this)
                    .command(fromKey(key, def))
                    .register()));

            StorageType storageType = PluginSettings.STATISTICS_STORAGE_TYPE.get();

            if (storageType == StorageType.SQLITE && Bukkit.getPluginManager().getPlugin("SpleefXSQL") == null)
                SpleefX.logger().warning("The storage type is SQLite, but SpleefXSQL extension is not found. Defaulting to flat file storage");

            dataProvider = storageType.create();
            dataProvider.createRequiredFiles(fileManager);

            if (storageType == StorageType.UNITED_FILE) {
                new StorageTypeConverter(dataProvider).run();
                SpleefX.logger().warning("I noticed you're using UNITED_FILE as a storage type. This is no longer supported as it cannot work with all the new data it has to store. Player data has been converted to use FLAT_FILE instead.");
            }

            if (GameStats.VAULT_EXISTS.get())
                vaultHandler = new VaultHandler(this);

            Bukkit.getScheduler().runTaskTimer(this, () -> GameArena.ARENAS.get().values().forEach(arena -> arena.getEngine().getSignManager().update()),
                    ((Integer) PluginSettings.SIGN_UPDATE_INTERVAL.get()).longValue(), ((Integer) PluginSettings.SIGN_UPDATE_INTERVAL.get()).longValue());
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                saveArenas();
                messageManager.save();
                dataProvider.saveEntries(this);
                //PluginSettings.save();
            }, 24000, 24000); // 20 minutes
            getServer().getPluginManager().registerEvents(new DoubleJumpHandler(this, abilityDelays), this);
            getServer().getPluginManager().registerEvents(new ScoreboardListener(), this);
            getServer().getPluginManager().registerEvents(new GameMenu.MenuListener(), this);
            getServer().getPluginManager().registerEvents(new BoosterFactory.BoosterListener(), this);
            // AntiCheat hooks
            if (getServer().getPluginManager().getPlugin("Spartan") != null) {
                new SpartanHook(this);
            }
            PERKS.values().stream().filter(v -> v instanceof Listener).forEach(v -> getServer().getPluginManager().registerEvents((Listener) v, this));
            abilityDelays.start();
            activeBoosterLoader.getActiveBoosters().forEach((player, booster) -> {
                if (booster != null) {
                    if (player.isOnline() || !player.isOnline() && BoosterFactory.CONSUME_WHILE_OFFLINE.get())
                        booster.activate(player);
                    else
                        booster.pause();
                }
            });
            boosterConsumer.start(this);

            scoreboardTicker = new ScoreboardTicker();
            scoreboardTicker.setTicks(((Number) PluginSettings.SCOREBOARD_UPDATE_INTERVAL.get()).intValue());

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                if (PlaceholderAPI.unregisterPlaceholderHook("SpleefX")) {
                    new OldExpansionRemover(this).run();
                    logger().info("Removed old SpleefX-PAPI jar to avoid conflict.");
                }
                logger().info("Found PlaceholderAPI. Registering hooks");
                new SpleefXPAPI(this).register();
            }

            getLogger().info("Establishing connection to bstats.org");
            Metrics metrics = new Metrics(this, 7694);
            metrics.addCustomChart(new AdvancedPie("most_used_modes", () -> {
                Map<String, Integer> m = new HashMap<>(BSTATS_EXTENSIONS);
                BSTATS_EXTENSIONS.clear();
                return m;
            }));
            //scheduler = new SpleefXScheduler(this);
        } catch (Exception e) {
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                File crashLog = new File(getDataFolder(), "crash.log");
                if (!crashLog.exists()) crashLog.createNewFile();
                FileWriter writer = new FileWriter(crashLog, false);
                writer.write(sw.toString());
                writer.close();
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }

            SpleefX.logger().severe("Failed to enable plugin. Error has been dumped to /SpleefX/crash.log. Please send the file over on our Discord server for support.");
            CompatibilityHandler.disable();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (CompatibilityHandler.shouldDisable() || CompatibilityHandler.missingWorldEdit() || CompatibilityHandler.missingProtocolLib())
            return;
        try {
            disableArenas();
        } catch (Exception e) {
            logger().warning("Failed to regenerate arenas.");
            e.printStackTrace();
        }
        boosterConsumer.cancel();
        saveArenas();
        messageManager.save();
        dataProvider.saveEntries(this);
        statsFile.save();
        boostersFile.save();
        try {
            configurationPack.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        joinGuiFile.save();
        getLogger().info("\"The last enemy that shall be destroyed is death...\"");
    }

    /**
     * {@inheritDoc}
     */
    public static SchematicManager newSchematicManager(String name) {
        return SchematicManager.newSchematicManager(plugin.worldEdit, name, plugin.arenasFolder);
    }

    private void saveArenas() {
        arenasConfig.save();
        DirectConfiguration c = DirectConfiguration.of(arenasConfig.getFile());
        Map<String, JsonElement> elements = new LinkedHashMap<>();
        GameArena.ARENAS.get().forEach((key, arena) -> elements.put(key, ArenaData.GSON.toJsonTree(arena)));
        elements.putAll(GameArenaAdapter.REMOVED_ARENAS);
        c.set("arenas", elements);
        c.save(Throwable::printStackTrace);
    }

    /**
     * Invoked when the server stops, to restore everything and end games forcibly
     */
    private void disableArenas() {
        GameArena.ARENAS.get().values().stream().filter(arena -> arena.getStage().isEndable())
                .forEach(arena -> arena.getEngine().forceEnd());
    }

    {
        plugin = this;
        pluginLogger = getLogger();
        if (!CompatibilityHandler.shouldDisable()) {
            File ext = new File(getDataFolder(), "extensions");
            fileManager.createFile("config.yml");
            new ConfigConverter(new File(getDataFolder(), "config.yml")).run();
            new LegacyExtensionConverter(ext).run();
            new SpleefExtensionConverter(ext).run();
            new SpleggExtensionConverter(ext).run();
            new SpectatorFileConverter(getDataFolder()).run();
            //saveDefaultConfig();
            fileManager.createDirectory("extensions");
            fileManager.createDirectory("perks");
            fileManager.createDirectory("extensions" + separator + "custom");
            fileManager.createDirectory("extensions" + separator + "standard");
            fileManager.createFile("extensions" + separator + "standard" + separator + "spleef.json");
            fileManager.createFile("extensions" + separator + "standard" + separator + "bow_spleef.json");
            fileManager.createFile("extensions" + separator + "standard" + separator + "splegg.json");
            fileManager.createFile("extensions" + separator + "custom" + separator + "-example-mode.json");
            fileManager.createFile("perks" + separator + "-perks-shop.json");
            fileManager.createFile("perks" + separator + "acidic_snowballs.json");
            fileManager.createFile("spectator-settings.json");
        }
    }

    private static CommandExecutor fromKey(final String key, final CommandExecutor def) {
        switch (key) {
            case "bow_spleef":
                return new CommandBowSpleef();
            case "spleef":
                return new CommandSpleef();
            case "splegg":
                return new CommandSplegg();
            default: {
                return def;
            }
        }
    }

    public static SpectatorSettings getSpectatorSettings() {
        return spectatorMenu;
    }

    @SuppressWarnings("RedundantTypeArguments")
    public static RuntimeException sneakyThrow(@NotNull Throwable t) {
        return SpleefX.<RuntimeException>sneakyThrow0(t);
    }

    private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }

    public static Logger logger() {
        return plugin.pluginLogger;
    }

    private static Thread mainThread = null;

}
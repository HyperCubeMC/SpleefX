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
import io.github.spleefx.compatibility.worldedit.SchematicManager;
import io.github.spleefx.config.SpleefXConfig;
import io.github.spleefx.converter.ConfigConverter;
import io.github.spleefx.converter.LegacyExtensionConverter;
import io.github.spleefx.converter.SpectatorFileConverter;
import io.github.spleefx.converter.SpleggExtensionConverter;
import io.github.spleefx.data.PlayerRepository;
import io.github.spleefx.data.PlayerRepository.QueryListener;
import io.github.spleefx.data.SpleefXPAPI;
import io.github.spleefx.data.menu.StatisticsConfig;
import io.github.spleefx.dep.Dependency;
import io.github.spleefx.dep.DependencyManager;
import io.github.spleefx.dep.classloader.ReflectionClassLoader;
import io.github.spleefx.economy.booster.ActiveBoosterLoader;
import io.github.spleefx.economy.booster.BoosterConsumer;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.GameExtension.ExtensionType;
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
import io.github.spleefx.util.FileWatcher;
import io.github.spleefx.util.io.CopyStore;
import io.github.spleefx.util.io.FileManager;
import io.github.spleefx.util.menu.GameMenu;
import io.github.spleefx.util.message.message.MessageImporter;
import io.github.spleefx.util.message.message.MessageManager;
import io.github.spleefx.util.plugin.DelayExecutor;
import io.github.spleefx.util.plugin.Protocol;
import io.github.spleefx.vault.VaultHandler;
import lombok.Getter;
import lombok.SneakyThrows;
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
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

import static io.github.spleefx.dep.Dependency.*;
import static io.github.spleefx.extension.ExtensionsManager.EXTENSIONS_FOLDER;
import static io.github.spleefx.perk.GamePerk.PERKS;
import static io.github.spleefx.perk.GamePerk.PERKS_FOLDER;
import static io.github.spleefx.util.FileWatcher.registerWatcher;
import static java.io.File.separator;
import static org.moltenjson.configuration.tree.strategy.TreeNamingStrategy.STRING_STRATEGY;

@SuppressWarnings("FieldMayBeFinal")
@Getter
public final class SpleefX extends JavaPlugin implements Listener {

    public static final Map<String, Integer> BSTATS_EXTENSIONS = new ConcurrentHashMap<>();

    /**
     * This will allow us to run separately from {@link ForkJoinPool#commonPool()} which is
     * used badly by some plugins.
     */
    public static final ForkJoinPool POOL = new ForkJoinPool();

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

    private boolean protocolLib;
    private WorldEditPlugin worldEdit;
    private ArenaManager arenaManager;
    private VaultHandler vaultHandler;
    private SelectableConfiguration arenasConfig;

    private final DelayExecutor<GameAbility> abilityDelays = new DelayExecutor<>(this);
    private final BoosterConsumer boosterConsumer = new BoosterConsumer();
    private final FileManager<SpleefX> fileManager = new FileManager<>(this);
    private final Logger pluginLogger;

    private final SelectableConfiguration statsFile = SelectableConfiguration.of(JsonFile.of(
            registerWatcher(fileManager.createFile("gui" + separator + "statistics-gui.json"))
                    .onChange(path -> getStatsFile().refresh())
                    .getFile()), false, AdapterBuilder.GSON);

    private final SelectableConfiguration boostersFile = SelectableConfiguration.of(JsonFile.of(
            registerWatcher(fileManager.createFile("boosters" + separator + "boosters.json"))
                    .onChange(path -> getBoostersFile().refresh()).getFile()), false, AdapterBuilder.GSON);

    private final SelectableConfiguration joinGuiFile = SelectableConfiguration.of(JsonFile.of(
            registerWatcher(fileManager.createFile("gui" + separator + "join-gui.json"))
                    .onChange((p) -> getJoinGuiFile().refresh())
                    .getFile()), false, AdapterBuilder.GSON);

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

    @Getter
    private ReflectionClassLoader reflectionClassLoader = new ReflectionClassLoader(this);

    public void loadMissing() {
        if (downloadIfMissing("ProtocolLib", "https://github.com/dmulloy2/ProtocolLib/releases/download/4.5.1/ProtocolLib.jar"))
            protocolLib = true;
    }

    @Override
    public void onLoad() {
        plugin = this;
        DependencyManager dependencyManager = new DependencyManager(this);
        EnumSet<Dependency> deps = EnumSet.of(OKIO, OKHTTP, CAFFEINE, HIKARI);
        if (Protocol.PROTOCOL == 8)
            deps.add(GSON);
        dependencyManager.loadDependencies(deps);
        dependencyManager.loadStorageDependencies(io.github.spleefx.data.StorageType.fromName(Objects.requireNonNull(getConfig().getString("StorageMethod", "H2"))));
    }

    @SneakyThrows
    private boolean downloadIfMissing(String plugin, String url) {
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
            Bukkit.getPluginManager().loadPlugin(pluginJAR).onLoad();
            return true;
        }
        return false;
    }

    @Override
    public void onEnable() {
        loadMissing();
        if (protocolLib)
            Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().getPlugin("ProtocolLib"));
        if (CompatibilityHandler.shouldDisable()) {
            SpleefX.logger().severe("Unsupported server protocol: 1." + Protocol.EXACT);
            SpleefX.logger().severe("Please use one of the following: 1.8.8, 1.8.9, 1.12.2, 1.13.2, 1.14.X, 1.15.X for the plugin to function");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (CompatibilityHandler.missingWorldEdit()) {
            String v = "6.1.9";
            String d = "https://dev.bukkit.org/projects/worldedit/files/2597538/download";
            if (Protocol.isNewerThan(13)) { // 1.13+
                v = "7.0.0";
                d = "https://dev.bukkit.org/projects/worldedit/files/2723275/download";
            }
            if (Protocol.EXACT >= 14.4) { // 1.14.4 or greater
                v = "latest";
                d = "https://dev.bukkit.org/projects/worldedit/files/latest";
            }
            SpleefX.logger().severe("No WorldEdit found. Please download WorldEdit (" + v + "), from " + d);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            getLogger().info("Detected server version: " + Protocol.VERSION);

            messageManager = new MessageManager(this);
            messageManager.load(false);

            new MessageImporter(this);

            CompatibilityHandler.init();
            arenaManager = new ArenaManager(this);

            SpleefXConfig.load(true);
            //fileManager.createDirectory(SpleefXConfig.STATISTICS_DIRECTORY.get());
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
            p.registerEvents(new QueryListener(), this);

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
            if (Protocol.PROTOCOL != 8)
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

            /*
TODO
            dataProvider = storageType.create();
            dataProvider.createRequiredFiles(fileManager);

            if (storageType == StorageType.UNITED_FILE) {
                SpleefX.logger().warning("I noticed you're using UNITED_FILE as a storage type. This is no longer supported as it cannot work with all the new data_old it has to store. Player data_old has been converted to use FLAT_FILE instead.");
            }

*/
            if (SpleefXConfig.VAULT_EXISTS)
                vaultHandler = new VaultHandler(this);


            Bukkit.getScheduler().runTaskTimer(this, () -> GameArena.ARENAS.get().values().forEach(arena -> arena.getEngine().getSignManager().update()),
                    SpleefXConfig.SIGN_UPDATE_INTERVAL.get().longValue(), SpleefXConfig.SIGN_UPDATE_INTERVAL.get().longValue());
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                saveArenas();
                messageManager.save();
                PlayerRepository.REPOSITORY.save();
                //PluginSettings.save();
            }, 24000, 24000); // 20 minutes
            getServer().getPluginManager().registerEvents(new DoubleJumpHandler(abilityDelays), this);
            getServer().getPluginManager().registerEvents(new ScoreboardListener(), this);
            getServer().getPluginManager().registerEvents(new GameMenu.MenuListener(), this);
            getServer().getPluginManager().registerEvents(new BoosterFactory.BoosterListener(), this);
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
            scoreboardTicker.setTicks(((Number) SpleefXConfig.SCOREBOARD_UPDATE_INTERVAL.get()).intValue());

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
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
                new ProtocolLibSpectatorAdapter(this);
            FileWatcher.pollDirectory(getDataFolder().toPath());
            PlayerRepository.REPOSITORY.cacheAll();
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
        try {
            FileWatcher.getService().close();
            FileWatcher.getPool().shutdownNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (CompatibilityHandler.shouldDisable() || CompatibilityHandler.missingWorldEdit() || CompatibilityHandler.missingProtocolLib())
            return;
        PlayerRepository.REPOSITORY.save();
        try {
            disableArenas();
        } catch (Exception e) {
            logger().warning("Failed to regenerate arenas.");
            e.printStackTrace();
        }
        boosterConsumer.cancel();
        saveArenas();
        messageManager.save();
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
            registerWatcher(fileManager.createFile("config.yml"))
                    .onChange(path -> {
                        SpleefX.getPlugin().reloadConfig();
                        SpleefXConfig.load(false);
                    });
            new ConfigConverter(new File(getDataFolder(), "config.yml")).run();
            new LegacyExtensionConverter(ext).run();
            //new SpleefExtensionConverter(ext).run();
            new SpleggExtensionConverter(ext).run();
            new SpectatorFileConverter(getDataFolder()).run();
            //saveDefaultConfig();
            fileManager.createDirectory("extensions");
            fileManager.createDirectory("perks");
            fileManager.createDirectory("extensions" + separator + "custom");
            fileManager.createDirectory("extensions" + separator + "standard");

            registerWatcher(fileManager.createFile("extensions" + separator + "standard" + separator + "spleef.json"))
                    .onChange(path -> ExtensionsManager.getByKey("spleef").refresh(ExtensionType.STANDARD));

            registerWatcher(fileManager.createFile("extensions" + separator + "standard" + separator + "bow_spleef.json"))
                    .onChange(path -> ExtensionsManager.getByKey("bow_spleef").refresh(ExtensionType.STANDARD));

            registerWatcher(fileManager.createFile("extensions" + separator + "standard" + separator + "splegg.json"))
                    .onChange(path -> ExtensionsManager.getByKey("splegg").refresh(ExtensionType.STANDARD));

            registerWatcher(fileManager.createFile("spectator-settings.json"))
                    .onChange(path -> SpleefX.getPlugin().getConfigurationPack().refresh());

            fileManager.createFile("extensions" + separator + "custom" + separator + "-example-mode.json");
            registerWatcher(fileManager.createFile("perks" + separator + "-perks-shop.json"));
            fileManager.createFile("perks" + separator + "acidic_snowballs.json");

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

}
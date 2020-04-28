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
import io.github.spleefx.command.parent.CommandsContainer;
import io.github.spleefx.command.parent.extensions.StatsCommand;
import io.github.spleefx.command.plugin.PluginCommandBuilder;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.compatibility.worldedit.SchematicProcessor;
import io.github.spleefx.converter.*;
import io.github.spleefx.data.DataProvider;
import io.github.spleefx.data.DataProvider.StorageType;
import io.github.spleefx.data.GameStats;
import io.github.spleefx.data.StatisticsConfig;
import io.github.spleefx.economy.booster.ActiveBoosterLoader;
import io.github.spleefx.economy.booster.BoosterConsumer;
import io.github.spleefx.economy.booster.BoosterFactory;
import io.github.spleefx.extension.ExtensionsManager;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.extension.ability.DoubleJumpHandler;
import io.github.spleefx.extension.ability.GameAbility;
import io.github.spleefx.extension.ability.TripleArrowsAbility;
import io.github.spleefx.gui.JoinGUI.MenuSettings;
import io.github.spleefx.gui.MessageGUI.ChatListener;
import io.github.spleefx.listeners.ArenaListener;
import io.github.spleefx.listeners.ConnectionListener;
import io.github.spleefx.listeners.RenameListener;
import io.github.spleefx.listeners.SignListener;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.perk.GamePerk;
import io.github.spleefx.perk.PerkShop;
import io.github.spleefx.scoreboard.GameScoreboardAdapter;
import io.github.spleefx.scoreboard.ScoreboardListener;
import io.github.spleefx.util.io.CopyStore;
import io.github.spleefx.util.io.FileManager;
import io.github.spleefx.util.menu.GameMenu;
import io.github.spleefx.util.plugin.DelayExecutor;
import io.github.spleefx.util.plugin.PluginSettings;
import io.github.spleefx.util.plugin.Protocol;
import io.github.spleefx.vault.VaultHandler;
import io.github.thatkawaiisam.assemble.Assemble;
import io.github.thatkawaiisam.assemble.AssembleStyle;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.moltenjson.adapter.GameArenaAdapter;
import org.moltenjson.configuration.direct.DirectConfiguration;
import org.moltenjson.configuration.pack.ConfigurationPack;
import org.moltenjson.configuration.pack.DeriveFrom;
import org.moltenjson.configuration.select.SelectableConfiguration;
import org.moltenjson.configuration.tree.TreeConfiguration;
import org.moltenjson.configuration.tree.TreeConfigurationBuilder;
import org.moltenjson.json.JsonFile;
import org.moltenjson.utils.AdapterBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static io.github.spleefx.extension.ExtensionsManager.EXTENSIONS_FOLDER;
import static io.github.spleefx.perk.GamePerk.PERKS;
import static io.github.spleefx.perk.GamePerk.PERKS_FOLDER;
import static java.io.File.separator;
import static org.moltenjson.configuration.tree.strategy.TreeNamingStrategy.STRING_STRATEGY;

public final class SpleefX extends JavaPlugin implements Listener {

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
    private static SpleefX plugin;

    /**
     * WorldEdit plugin instance
     */
    private WorldEditPlugin worldEdit;

    private DelayExecutor<GameAbility> abilityDelays = new DelayExecutor<>(this);

    /**
     * Compatibility handler for different WorldEdit versions
     */
    private CompatibilityHandler compatibilityHandler;

    /**
     * The arena manager instance
     */
    private ArenaManager arenaManager;

    /**
     * The booster consumer
     */
    private BoosterConsumer boosterConsumer = new BoosterConsumer();

    /**
     * Vault handler
     */
    private VaultHandler vaultHandler;

    /**
     * The file manager
     */
    private final FileManager<SpleefX> fileManager = new FileManager<>(this);

    /**
     * The plugin logger
     */
    private Logger pluginLogger;

    /**
     * Arenas configuration
     */
    private SelectableConfiguration arenas;

    /**
     * Statistics config
     */
    private SelectableConfiguration statsFile = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("gui" + separator + "statistics-gui.json")), false, AdapterBuilder.GSON);

    /**
     * Boosters config
     */
    private final SelectableConfiguration boostersFile = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("boosters" + separator + "boosters.json")), false, AdapterBuilder.GSON);

    @DeriveFrom("boosters/active-boosters.json")
    private static ActiveBoosterLoader activeBoosterLoader = new ActiveBoosterLoader();

    @DeriveFrom("perks/-perks-shop.json")
    private static PerkShop perkShop = new PerkShop("&ePerk Shop", 3, new HashMap<>());

    private final ConfigurationPack<SpleefX> configurationPack = new ConfigurationPack<>(this, getDataFolder(), ArenaData.GSON);

    /**
     * Join gui config
     */
    private final SelectableConfiguration joinGuiFile = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("gui" + separator + "join-gui.json")), false, AdapterBuilder.GSON);

    private Assemble assemble;

    /**
     * The folder that contains arena schematics and arenas.json
     */
    private final File arenasFolder = new File(getDataFolder(), "arenas");

    /**
     * The extensions controller
     */
    private ExtensionsManager extensionsManager;

    /**
     * The data provider
     */
    private DataProvider dataProvider;

    @Override
    public void onEnable() {
        if (CompatibilityHandler.shouldDisable()) {
            SpleefX.logger().severe("Unsupported server protocol: 1." + Protocol.EXACT);
            SpleefX.logger().severe("Please update to at least 1.8 for the plugin to function");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!CompatibilityHandler.hasWorldEdit()) {
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
        compatibilityHandler = new CompatibilityHandler(this);
        arenaManager = new ArenaManager(this);

        fileManager.createFile("messages.json");
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

        FileConfiguration arenaTypes = YamlConfiguration.loadConfiguration(fileManager.createFile("arenas" + separator + "arena-types.yml"));
        for (String className : arenaTypes.getStringList("ArenaSubTypes")) {
            try {
                Class<?> c = Class.forName(className);
                if (GameArena.class.isAssignableFrom(c)) {
                    //noinspection unchecked
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
        //SpleggExtension.EXTENSION = ExtensionsManager.getExtension("splegg", ExtensionType.STANDARD, SpleggExtension.class);
        Map<String, GameExtension> data = extensions.load(GameExtension.class);
        try {
            PERKS.putAll(perks.load(GamePerk.class));
            configurationPack.updateField("perkShop");
        } catch (IOException e) {
            e.printStackTrace();
        }
        extensionsManager = new ExtensionsManager(this);
        arenas = SelectableConfiguration.of(JsonFile.of(arenasFolder, "arenas.json"), false, ArenaData.GSON)
                .register(GameArena.class).associate();
        int original = GameArena.ARENAS.get().size();
        GameArena.ARENAS.get().values().removeIf(Objects::isNull); // Filter out arenas which couldn't be loaded
        int modified = GameArena.ARENAS.get().size();
        logger().info("Successfully loaded " + modified + " arena" + (modified == 1 ? "" : "s") + " out of " + original);
        final PluginManager p = Bukkit.getPluginManager();
        MessageKey.load(false);

        p.registerEvents(new ChatListener(), this);
        p.registerEvents(new io.github.spleefx.util.menu.MenuListener(), this);
        p.registerEvents(new TripleArrowsAbility(abilityDelays), this);

        p.registerEvents(new SignListener(this), this);
        p.registerEvents(new ConnectionListener(), this);
        p.registerEvents(new RenameListener(), this);
        p.registerEvents(new ArenaListener(), this);
        p.registerEvents(new CopyStore(), this);
        p.registerEvents(new BowSpleefListener(this), this);
        p.registerEvents(new SpleggListener(this), this);
        p.registerEvents(new SpleefListener(), this);
        p.registerEvents(new StatsCommand.MenuListener(), this);

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
            p.registerEvents(new ArenaListener.WGListener(this), this);
        else
            p.registerEvents(new ArenaListener.BlockBreakListener(this), this);

        Preconditions.checkNotNull(getCommand("spleefx")).setExecutor(CommandsContainer.SPLEEFX);

        CommandExecutor def = CommandsContainer.CUSTOM;

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
            MessageKey.save();
            dataProvider.saveEntries(this);
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

        assemble = new Assemble(this, new GameScoreboardAdapter());
        assemble.setTicks(((Number) PluginSettings.SCOREBOARD_UPDATE_INTERVAL.get()).intValue());
        assemble.setAssembleStyle(AssembleStyle.KOHI);

    }

    @Override
    public void onDisable() {
        if (CompatibilityHandler.shouldDisable() || !CompatibilityHandler.hasWorldEdit()) return;
        try {
            disableArenas();
        } catch (Exception e) {
            logger().warning("Failed to regenerate arenas.");
            e.printStackTrace();
        }
        boosterConsumer.cancel();
        saveArenas();
        MessageKey.save();
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
     * Returns the WorldEdit instance
     *
     * @return WorldEdit plugin instance
     */
    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }

    /**
     * Returns the arenas folder
     *
     * @return The arenas folder
     */
    public File getArenasFolder() {
        return arenasFolder;
    }

    public Assemble getAssemble() {
        return assemble;
    }

    public static ActiveBoosterLoader getActiveBoosterLoader() {
        return activeBoosterLoader;
    }

    public static PerkShop getPerkShop() {
        return perkShop;
    }

    /**
     * Returns the arena manager
     *
     * @return The arena manager
     */
    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    /**
     * Returns the data provider
     *
     * @return The data provider
     */
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public SelectableConfiguration getStatsFile() {
        return statsFile;
    }

    public ExtensionsManager getExtensionsManager() {
        return extensionsManager;
    }

    public BoosterConsumer getBoosterConsumer() {
        return boosterConsumer;
    }

    /**
     * Returns the plugin instance
     *
     * @return Plugin instance
     */
    public static SpleefX getPlugin() {
        return plugin;
    }

    /**
     * {@inheritDoc}
     */
    public static SchematicProcessor newSchematicProcessor(String name) {
        return plugin.compatibilityHandler.create(name);
    }

    private void saveArenas() {
        arenas.save();
        DirectConfiguration c = DirectConfiguration.of(arenas.getFile());
        Map<String, JsonElement> elements = new LinkedHashMap<>();
        GameArena.ARENAS.get().forEach((key, arena) -> elements.put(key, ArenaData.GSON.toJsonTree(arena)));
        elements.putAll(GameArenaAdapter.REMOVED_ARENAS);
        c.set("arenas", elements);
        c.save(Throwable::printStackTrace);
    }

    public SelectableConfiguration getArenasConfig() {
        return arenas;
    }

    public SelectableConfiguration getJoinGuiFile() {
        return joinGuiFile;
    }

    /**
     * Invoked when the server stops, to restore everything and end games forcibly
     */
    private void disableArenas() {
        GameArena.ARENAS.get().values().forEach(arena -> arena.getEngine().forceEnd());
    }

    {
        plugin = this;
        pluginLogger = getLogger();
        if (!CompatibilityHandler.shouldDisable()) {
            File ext = new File(getDataFolder(), "extensions");
            fileManager.createFile("config.yml");
            new ConfigConverter(new File(getDataFolder(), "config.yml")).run();
            new MessageFileConverter(new File(getDataFolder(), "messages.json")).run();
            new LegacyExtensionConverter(ext).run();
            new SpleefExtensionConverter(ext).run();
            new SpleggExtensionConverter(ext).run();
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
        }
    }

    private CommandExecutor fromKey(final String key, final CommandExecutor def) {
        switch (key) {
            case "bow_spleef": {
                return CommandsContainer.BOW_SPLEEF;
            }
            case "spleef": {
                return CommandsContainer.SPLEEF;
            }
            case "splegg": {
                return CommandsContainer.SPLEGG;
            }
            default: {
                return def;
            }
        }
    }

    public TreeConfiguration<String, GameExtension> getExtensions() {
        return extensions;
    }

    public VaultHandler getVaultHandler() {
        return vaultHandler;
    }

    public static Logger logger() {
        return plugin.pluginLogger;
    }

}
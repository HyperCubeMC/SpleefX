package io.github.spleefx.util.plugin;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A simple delay management utility
 *
 * @param <R> The plugin of this executor
 */
public class DelayExecutor<R extends Enum<R>> {

    /**
     * The global delay map
     */
    private final Map<UUID, Map<R, DelayData>> delayMap = new HashMap<>();

    /**
     * The plugin instance
     */
    private JavaPlugin plugin;

    /**
     * The delay task
     */
    private BukkitTask delayTask;

    /**
     * Creates a new delay executor for the specified plugin
     *
     * @param plugin Plugin to create for
     */
    public DelayExecutor(JavaPlugin plugin) {
        Preconditions.checkNotNull(plugin, "Plugin must not be null!");
        this.plugin = plugin;
    }

    /**
     * Adds delay for the specified player
     *
     * @param player  Player to add delay for
     * @param context Context for the delay
     * @param data    Delay data
     */
    public void setDelay(OfflinePlayer player, R context, DelayData data) {
        if (data.timeLeft == 0) return;
        getDelayMap(player.getUniqueId()).put(context, data);
    }

    /**
     * Forcibly cancels the delay of the specified player in the context
     *
     * @param player  Player to cancel for
     * @param context Context to remove
     */
    public void cancelDelay(OfflinePlayer player, R context) {
        getDelayMap(player.getUniqueId()).remove(context);
    }

    /**
     * Returns the time left for the player in the specified context, or {@code 0} if
     * the player has no delay in that context.
     *
     * @param player  Player to get for
     * @param context The context
     * @return The time left, or {@code 0} if the player has no delay.
     */
    public int getTimeLeft(OfflinePlayer player, R context) {
        DelayData delayData = getDelayMap(player.getUniqueId()).get(context);
        return delayData == null ? 0 : delayData.getTimeLeft();
    }

    /**
     * Returns whether does the player have delay in the specified context
     *
     * @param player  Player to check for
     * @param context Context to check for
     * @return {@code true} if the player does have context, {@code false} if otherwise.
     */
    public boolean hasDelay(OfflinePlayer player, R context) {
        return getDelayMap(player.getUniqueId()).containsKey(context);
    }

    /**
     * Returns the {@link DelayData} of the specified context from the specified player.
     *
     * @param player  Player to retrieve for
     * @param context The context
     * @return The delay data, or {@code null} if not found.
     */
    public DelayData getDelayData(OfflinePlayer player, R context) {
        return getDelayMap(player.getUniqueId()).get(context);
    }

    /**
     * Cancels all the players' delays
     */
    public void cancelAllDelays() {
        delayMap.clear();
    }

    /**
     * Cancels all delays for the specified player
     *
     * @param player Player to clear for
     */
    public void cancelAllDelays(OfflinePlayer player) {
        getDelayMap(player.getUniqueId()).clear();
    }

    /**
     * Cancels the BukkitTask responsible for reducing delays
     */
    public void cancelBukkitTask() {
        delayTask.cancel();
    }

    /**
     * Starts the reducing task
     */
    public void start() {
        delayTask = Bukkit.getScheduler().runTaskTimer(plugin, this::reduceAll, 20, 20);
    }

    /**
     * Returns the plugin of this delay executor
     *
     * @return The plugin
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Reduces all delay by 1
     */
    private void reduceAll() {
        delayMap.forEach((uuid, map) -> {
            for (Iterator<Entry<R, DelayData>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<R, DelayData> entry = iterator.next();
                DelayData delayData = entry.getValue();
                if (delayData.reduce() <= 0) {
                    iterator.remove();
                    delayData.finish(uuid);
                }
            }
        });
    }

    /**
     * Returns the delay map for the specified player
     *
     * @param player Player to retrieve for
     * @return The player map
     */
    private Map<R, DelayData> getDelayMap(UUID player) {
        return delayMap.computeIfAbsent(player, (uuid) -> new HashMap<>());
    }

    /**
     * Represents data for delays
     */
    public static class DelayData {

        /**
         * The time left for the delay
         */
        private int timeLeft;

        /**
         * Task executed when the delay is finished
         */
        private Consumer<OfflinePlayer> onFinish;

        /**
         * Other data stored by the delay
         */
        private Map<Object, Object> data = new HashMap<>();

        /**
         * Creates a new delay data
         *
         * @param timeLeft Time left for the delay
         */
        public DelayData(int timeLeft) {
            this.timeLeft = timeLeft;
        }

        /**
         * Creates a new delay data
         *
         * @param timeLeft Time left for the delay
         */
        public DelayData(double timeLeft, boolean ticks) {
            this.timeLeft = (int) (ticks ? timeLeft * 20 : timeLeft);
        }

        /**
         * Returns the time left for the delay
         *
         * @return The time left
         */
        public int getTimeLeft() {
            return timeLeft;
        }

        /**
         * Reduces the delay
         *
         * @return The time before getting reduced
         */
        protected int reduce() {
            return --timeLeft;
        }

        /**
         * Puts the specified data in the delay data map
         *
         * @param key   Key of the data
         * @param value Value of the data
         * @return This object instance
         */
        public DelayData data(Object key, Object value) {
            data.put(key, value);
            return this;
        }

        /**
         * Returns the specified object from the key
         *
         * @param key Key to retrieve from
         * @param <T> The object type
         * @return The object, or {@code null} if not found.
         */
        @SuppressWarnings("unchecked")
        public <T> T get(Object key) {
            return (T) data.get(key);
        }

        /**
         * Sets the task of finishing
         *
         * @param onFinish Task to run when the delay is over. Can be null.
         * @return This object instance.
         */
        public DelayData setOnFinish(Consumer<OfflinePlayer> onFinish) {
            this.onFinish = onFinish;
            return this;
        }

        /**
         * Invokes the finishing callback on the specified UUID
         *
         * @param uuid UUID to run on
         */
        public void finish(UUID uuid) {
            if (onFinish != null)
                onFinish.accept(Bukkit.getOfflinePlayer(uuid));
        }
    }

}
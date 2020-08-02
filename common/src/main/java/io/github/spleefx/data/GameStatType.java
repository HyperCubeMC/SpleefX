package io.github.spleefx.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents a game statistic type
 */
public enum GameStatType {

    /**
     * The total amount of games played
     */
    GAMES_PLAYED,

    /**
     * The wins
     */
    WINS,

    /**
     * The losses
     */
    LOSSES,

    /**
     * The draws
     */
    DRAWS,

    /**
     * The blocks that were mined or destroyed
     */
    BLOCKS_MINED,

    /**
     * Splegg shots
     */
    SPLEGG_SHOTS,

    /**
     * The amount of TNT exploded in splegg/bow spleef
     */
    TNT_EXPLODED,

    /**
     * Bow spleef arrow shots
     */
    BOW_SPLEEF_SHOTS,

    /**
     * The player score
     */
    SCORE;

    public static final GameStatType[] values = values();
    private static final Map<String, GameStatType> BY_NAME = new HashMap<>();
    private final String name;


    GameStatType() {
        name = name().toLowerCase();
    }

    static {
        for (GameStatType value : values) BY_NAME.put(value.name.toLowerCase(), value);
    }

    @Nullable
    public static GameStatType fromName(@NotNull String name) {
        requireNonNull(name, "name");
        return BY_NAME.get(name.toLowerCase());
    }
}
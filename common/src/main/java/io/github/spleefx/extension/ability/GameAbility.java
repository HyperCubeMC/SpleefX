package io.github.spleefx.extension.ability;

import java.util.Map;

/**
 * Represents a bow spleef ability
 */
public enum GameAbility {

    /**
     * Double jump ability
     */
    DOUBLE_JUMP,

    /**
     * Triple arrows ability
     */
    TRIPLE_ARROWS,

    /**
     * Rippler (launches nearby players into the air)
     */
    RIPPLER,

    /**
     * Arrow volley ability (shoot arrows in all directions)
     */
    ARROW_VOLLEY;

    /**
     * Reduces the ability count for the specified ability
     *
     * @param abilityCount Ability to reduce for
     * @return The new value
     */
    public int reduceAbility(Map<GameAbility, Integer> abilityCount) {
        int v = abilityCount.getOrDefault(this, 0);
        if (v == 0) abilityCount.remove(this);
        else
            abilityCount.put(this, v - 1);
        return v - 1;
    }

}
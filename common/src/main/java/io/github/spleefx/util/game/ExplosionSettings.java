package io.github.spleefx.util.game;

import com.google.gson.annotations.Expose;

/**
 * Represents the settings of an explosion
 */
public class ExplosionSettings {

    @Expose
    private final boolean enabled;

    @Expose
    private final boolean createFire;

    @Expose
    private final boolean breakBlocks;

    @Expose
    private final boolean particles;

    @Expose
    private final float yield;

    @Expose
    private final float power;

    public ExplosionSettings(boolean enabled, boolean createFire, boolean breakBlocks, boolean particles, float yield, float power) {
        this.enabled = enabled;
        this.createFire = createFire;
        this.breakBlocks = breakBlocks;
        this.particles = particles;
        this.yield = yield;
        this.power = power;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean createFire() {
        return createFire;
    }

    public boolean breakBlocks() {
        return breakBlocks;
    }

    public float getPower() {
        return power;
    }

    public boolean particles() {
        return particles;
    }

    public float getYield() {
        return yield;
    }

}
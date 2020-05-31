package io.github.spleefx.util.game;

import com.google.gson.annotations.Expose;

/**
 * Represents the settings of an explosion
 */
public class ExplosionSettings {

    @Expose
    private boolean enabled;

    @Expose
    private boolean createFire;

    @Expose
    private boolean breakBlocks;

    @Expose
    private boolean particles;

    @Expose
    private float yield;

    @Expose
    private float power;

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
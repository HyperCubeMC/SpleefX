/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.spleefx.economy.booster;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import io.github.spleefx.SpleefX;
import io.github.spleefx.data.PlayerRepository;
import io.github.spleefx.economy.booster.BoosterFactory.FactoryStringAdapter;
import io.github.spleefx.util.message.message.Message;
import io.github.spleefx.util.plugin.Duration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

import static io.github.spleefx.economy.booster.BoosterFactory.ALLOW_MULTIPLE;

/**
 * Represents a coins booster
 */
public class BoosterInstance {

    /**
     * The booster type
     */
    @Expose
    @JsonAdapter(FactoryStringAdapter.class)
    private final BoosterFactory type;

    /**
     * The booster owner
     */
    private final UUID owner;

    /**
     * Whether is the booster enabled or not
     */
    @Expose
    private BoosterState state;

    /**
     * The booster multiplier
     */
    @Expose
    private final double multiplier;

    /**
     * The booster's duration
     */
    @Expose
    private long duration;

    public BoosterInstance(UUID owner, BoosterFactory type, double multiplier, Duration duration) {
        this.owner = owner;
        this.type = type;
        state = BoosterState.AVAILABLE;
        this.multiplier = multiplier;
        this.duration = duration.getSeconds();
    }

    public BoosterFactory getType() {
        return type;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isActive() {
        return state == BoosterState.ACTIVE;
    }

    public BoosterState getState() {
        return state;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void reduce() {
        duration--;
    }

    public long getDuration() {
        return duration;
    }

    public void pause() {
        SpleefX.getPlugin().getBoosterConsumer().pauseBooster(this);
        state = BoosterState.PAUSED;
        SpleefX.getActiveBoosterLoader().getActiveBoosters().remove(Bukkit.getOfflinePlayer(owner), this);
    }

    public int applyMultiplier(int value) {
        return (int) Math.round(value * multiplier);
    }

    /**
     * Activates this booster.
     *
     * @param player The owner of this booster in a {@link Player} instance. seriously dont
     *               frick this up and use someone else.
     */
    public boolean activate(OfflinePlayer player) {
        if (ALLOW_MULTIPLE.get() < 0) { // ppl can have unlimited boosters activated
            if (isActive()) return false;
            state = BoosterState.ACTIVE;
            SpleefX.getPlugin().getBoosterConsumer().consumeBooster(player, this);
        } else {
            if (PlayerRepository.REPOSITORY.lookup(player).getActiveBoosters().size() >= ALLOW_MULTIPLE.get()) {
                if (player.isOnline()) {
                    Message.CANNOT_ACTIVATE_MORE.reply(player.getPlayer(), this);
                    return false;
                }
            } else {
                if (isActive()) return false;
                state = BoosterState.ACTIVE;
                SpleefX.getPlugin().getBoosterConsumer().consumeBooster(player, this);
                return true;
            }
        }
        return false;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoosterInstance that = (BoosterInstance) o;
        return Double.compare(that.multiplier, multiplier) == 0 &&
                duration == that.duration &&
                type.equals(that.type) &&
                owner.equals(that.owner) &&
                state == that.state;
    }

    @Override public int hashCode() {
        return Objects.hash(type, owner, state, multiplier, duration);
    }
}
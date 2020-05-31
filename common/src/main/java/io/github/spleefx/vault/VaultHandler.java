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
package io.github.spleefx.vault;

import io.github.spleefx.SpleefX;
import io.github.spleefx.util.plugin.PluginSettings;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.plugins.Economy_SpleefX;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import static org.bukkit.Bukkit.getServer;

public class VaultHandler {

    /**
     * The server economy
     */
    @Getter
    private Economy economy;

    /**
     * Registers the economy
     */
    public VaultHandler(SpleefX plugin) {
        Plugin vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null) {
            SpleefX.logger().info("Vault found. Handling hooks...");
            Economy spleefxEconomy = new Economy_SpleefX(plugin.getDataProvider());

            RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
            if (provider == null) {
                SpleefX.logger().info("No economy plugin found (other than SpleefX). Hooking");
            }
            if (PluginSettings.ECO_HOOK_INTO_VAULT.get()) {
                SpleefX.logger().info("SpleefX vault hook is enabled in the config. Hooking into vault");
                Bukkit.getServicesManager().register(Economy.class, economy = spleefxEconomy, vaultPlugin, ServicePriority.Normal);
            } else if (provider == null || provider.getProvider() == null && ((boolean) PluginSettings.ECO_USE_VAULT.get())) {
                SpleefX.logger().warning("Vault hook is enabled, however no Vault-supported economy plugin is found. Defaulting to SpleefX economy.");
                economy = spleefxEconomy;
            } else {
                economy = provider.getProvider();
                SpleefX.logger().info("Using \"" + economy.getName() + "\" economy system.");
            }
        }
    }

    public double getCoins(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public void add(OfflinePlayer player, int amount) {
        economy.depositPlayer(player, amount);
    }

    public void withdraw(OfflinePlayer player, int amount) {
        economy.withdrawPlayer(player, amount);
    }

}
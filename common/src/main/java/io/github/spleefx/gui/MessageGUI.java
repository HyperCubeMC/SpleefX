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
package io.github.spleefx.gui;

import io.github.spleefx.SpleefX;
import io.github.spleefx.message.MessageCategory;
import io.github.spleefx.message.MessageKey;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.Metas;
import io.github.spleefx.util.item.ItemFactory;
import io.github.spleefx.util.item.Items;
import io.github.spleefx.util.menu.Button;
import io.github.spleefx.util.menu.GameMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.ChatPaginator;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static io.github.spleefx.compatibility.CompatibilityHandler.getMaterialCompatibility;
import static io.github.spleefx.compatibility.material.MaterialCompatibility.SIGN;

public class MessageGUI extends GameMenu {

    private static final String CANCEL = "cancel-edit";

    private static final BiConsumer<Player, MessageKey> EDIT = (player, key) -> {
        Metas.set(player, "spleefx.message.edit", key);
        Chat.plugin(player, "&dType in the new value for &e" + key.getName() + "&d:");
        Chat.plugin(player, "&eTo cancel, type &b&l" + CANCEL + "&e.");
    };

    /**
     * Creates a new menu
     */
    public MessageGUI(MessageCategory category) {
        super(category.getTitle() + ChatColor.RESET, 4);
        List<MessageKey> messages = MessageKey.byCategory(category);
        for (int i = 0; i < messages.size(); i++) {
            MessageKey m = messages.get(i);
            setButton(new Button(i, ItemFactory.create(getMaterialCompatibility().get(SIGN))
                    .setName("&a" + m.getName())
                    .setLore(Arrays.stream(ChatPaginator.wordWrap(m.getDescription(), 33)).map(c -> ChatColor.WHITE + c).collect(Collectors.toList()))
                    .addLoreLine("")
                    .addLoreLine("&dLeft click &eto edit")
                    .addLoreLine("&dRight click &eto see current value")
                    .create())
                    .addAction(Button.CANCEL_ACTION)
                    .addAction(e -> {
                        if (e.isRightClick()) {
                            Chat.plugin(e.getWhoClicked(), "&eCurrent value: &a" + m.getText());
                        } else {
                            e.getWhoClicked().closeInventory();
                            EDIT.accept((Player) e.getWhoClicked(), m);
                        }
                    }));
        }
        if (category.ordinal() > 0)
            setButton(new Button(27, Items.PREVIOUS_PAGE)
                    .addAction(event -> {
                        Inventory i = new MessageGUI(MessageCategory.VALUES[category.ordinal() - 1]).createInventory();
                        event.getWhoClicked().openInventory(i);
                    }));
        if (category.ordinal() != MessageCategory.VALUES.length - 1) // If it is not last page
            setButton(new Button(35, Items.NEXT_PAGE).addAction(event -> {
                Inventory i = new MessageGUI(MessageCategory.VALUES[category.ordinal() + 1]).createInventory();
                event.getWhoClicked().openInventory(i);
            }));
    }

    public static class ChatListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().hasMetadata("spleefx.message.edit")) return;
            MessageKey key = Metas.get(event.getPlayer(), "spleefx.message.edit");
            if (key == null) return;
            if (event.getMessage().equals(CANCEL)) {
                event.setCancelled(true);
                event.getPlayer().removeMetadata("spleefx.message.edit", SpleefX.getPlugin());
                Chat.plugin(event.getPlayer(), "&aEditing has been cancelled.");
                return;
            }
            key.setText(Chat.colorize(event.getMessage()));
            event.setCancelled(true);
            Chat.plugin(event.getPlayer(), "&aValue of &e" + key.getName() + " &ahas been changed to &d" + event.getMessage());
            event.getPlayer().removeMetadata("spleefx.message.edit", SpleefX.getPlugin());
        }
    }

}

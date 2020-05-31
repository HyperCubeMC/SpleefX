package io.github.spleefx.util.item;

import io.github.spleefx.SpleefX;
import io.github.spleefx.compatibility.CompatibilityHandler;
import io.github.spleefx.util.game.Chat;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Easily create itemstacks, without messing your hands.
 * <i>Note that if you do use this in one of your projects, leave this notice.</i>
 * <i>Please do credit me if you do use this in one of your projects.</i>
 *
 * @author NonameSL
 */
@SuppressWarnings({"unused", "ConstantConditions"})
public class ItemFactory {

    /**
     * The modified itemstack
     */
    private final ItemStack is;

    /**
     * The modified item meta
     */
    private final ItemMeta im;

    /**
     * Create a new ItemFactory from scratch.
     *
     * @param m The material to create the ItemFactory with.
     */
    private ItemFactory(Material m) {
        this(m, 1);
    }

    /**
     * Create a new ItemFactory over an existing itemstack.
     *
     * @param is The itemstack to create the ItemFactory over.
     */
    private ItemFactory(ItemStack is) {
        this.is = is;
        im = is.getItemMeta();
    }

    /**
     * Create a new ItemFactory from scratch.
     *
     * @param m      The material of the item.
     * @param amount The amount of the item.
     */
    private ItemFactory(Material m, int amount) {
        is = new ItemStack(m, amount);
        im = is.getItemMeta();
    }

    /**
     * Adds a glowing effect to the item.
     *
     * @param flag Should it add the glow effect. Useful since the code will be connected so we don't need to add if statements
     */
    public ItemFactory addGlowEffect(boolean flag) {
        if (flag) {
            im.addEnchant(Enchantment.LURE, 1, true);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    /**
     * Change the durability of the item.
     *
     * @param dur The durability to set it to.
     */
    public ItemFactory setDurability(short dur) {
        ((Damageable) im).setDamage(dur);
        return this;
    }

    /**
     * Set the display name of the item.
     *
     * @param name The name to change it to.
     */
    public ItemFactory setName(String name) {
        im.setDisplayName(colorize(name));
        return this;
    }

    /**
     * Add an unsafe enchantment.
     *
     * @param ench  The enchantment to add.
     * @param level The level to put the enchant on.
     */
    public ItemFactory addUnsafeEnchantment(Enchantment ench, int level) {
        is.addUnsafeEnchantment(ench, level);
        return this;
    }

    /**
     * Remove a certain enchant from the item.
     *
     * @param ench The enchantment to remove
     */
    public ItemFactory removeEnchantment(Enchantment ench) {
        is.removeEnchantment(ench);
        return this;
    }

    /**
     * Add an enchant to the item.
     *
     * @param ench  The enchant to add
     * @param level The level
     */
    public ItemFactory addEnchant(Enchantment ench, int level) {
        im.addEnchant(ench, level, true);
        return this;
    }

    /**
     * Add multiple enchants at once.
     *
     * @param enchantments The enchants to add.
     */
    public ItemFactory addEnchantments(Map<Enchantment, Integer> enchantments) {
        if (enchantments == null) return this;
        ;
        enchantments.forEach((e, p) -> im.addEnchant(e, p, true));
        is.setItemMeta(im);
        return this;
    }

    /**
     * Sets infinity durability on the item by setting the durability to Short.MAX_VALUE.
     */
    public ItemFactory setUnbreakable() {
        return setUnbreakable(true);
    }

    /**
     * Sets the item to be unbreakable
     */
    public ItemFactory setUnbreakable(boolean e) {
        return CompatibilityHandler.either(() -> {
            im.setUnbreakable(e);
            return this;
        }, () -> CompatibilityHandler.either(() -> {
            im.spigot().setUnbreakable(e);
            return this;
        }, () -> this));
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemFactory setLore(String... lore) {
        if (lore == null) return this;
        setLore(Arrays.asList(lore));
        return this;
    }

    /**
     * Re-sets the lore.
     *
     * @param lore The lore to set it to.
     */
    public ItemFactory setLore(List<String> lore) {
        if (lore == null) return this;
        IntStream.range(0, lore.size()).forEachOrdered(i -> lore.set(i, colorize(lore.get(i))));
        im.setLore(lore);
        return this;
    }

    /**
     * Adds the specified item flags
     *
     * @param flags Flags to add
     * @return This factory
     */
    public ItemFactory addItemFlags(List<ItemFlag> flags) {
        if (flags == null) return this;
        flags.forEach(im::addItemFlags);
        is.setItemMeta(im);
        return this;
    }

    /**
     * Remove a lore line.
     *
     * @param line The lore to remove.
     */
    public ItemFactory removeLoreLine(String line) {
        if (line == null) return this;
        List<String> lore = new ArrayList<>(im.getLore());
        lore.remove(line);
        setLore(lore);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     */
    public ItemFactory addLoreLine(String line) {
        if (line == null) return this;
        List<String> lore = new ArrayList<>();
        if (im.hasLore()) lore = new ArrayList<>(im.getLore());
        lore.add(line);
        setLore(lore);
        return this;
    }

    /**
     * Add a lore line.
     *
     * @param line The lore line to add.
     * @param pos  The index of where to put it.
     */
    public ItemFactory addLoreLine(String line, int pos) {
        if (line == null) return this;
        line = Chat.colorize(line);
        List<String> lore = new ArrayList<>(im.getLore());
        lore.set(pos, line);
        im.setLore(lore);
        return this;
    }

    /**
     * Sets the armor color of a leather armor piece. Works only on leather armor pieces.
     *
     * @param color The color to set it to.
     */
    public ItemFactory setArmorColor(Color color) {
        if (color == null) return this;
        try {
            LeatherArmorMeta im = (LeatherArmorMeta) this.im;
            im.setColor(color);
            is.setItemMeta(im);
        } catch (ClassCastException e) {
            SpleefX.logger().warning(e.getMessage());
        }
        return this;
    }

    /**
     * Sets the item amount
     *
     * @param amount New amount to set.
     */
    public ItemFactory setAmount(int amount) {
        is.setAmount(amount);
        return this;
    }

    /**
     * Retrieves the itemstack from the ItemFactory.
     *
     * @return The itemstack created/modified by the ItemFactory instance.
     */
    public ItemStack create() {
        is.setItemMeta(im);
        return is;
    }

    /**
     * Retrieves the itemstack from the ItemFactory.
     *
     * @return The itemstack created/modified by the ItemFactory instance.
     */
    public ItemStack copy(int amount) {
        return new ItemFactory(create()).setAmount(amount).create();
    }

    /**
     * Retrieves the itemstack from the ItemFactory.
     *
     * @return The itemstack created/modified by the ItemFactory instance.
     */
    public ItemFactory copy() {
        return new ItemFactory(create());
    }

    /**
     * Returns the type of this item
     *
     * @return The item type
     */
    public Material getType() {
        return is.getType();
    }

    /**
     * Colors the specified text
     *
     * @param text Text to color
     * @return The colored text
     */
    private static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Create a new ItemFactory over an existing itemstack.
     *
     * @param item The itemstack to create the ItemFactory over.
     * @return The factory
     */
    public static ItemFactory create(ItemStack item) {
        return new ItemFactory(item);
    }

    /**
     * Create a new ItemFactory from scratch.
     *
     * @param m      The material of the item.
     * @param amount The amount of the item.
     * @return The factory
     */
    public static ItemFactory create(Material m, int amount) {
        return new ItemFactory(m, amount);
    }

    /**
     * Create a new ItemFactory from scratch.
     *
     * @param m The material to create the ItemFactory with.
     * @return The factory
     */
    public static ItemFactory create(Material m) {
        return new ItemFactory(m);
    }
}
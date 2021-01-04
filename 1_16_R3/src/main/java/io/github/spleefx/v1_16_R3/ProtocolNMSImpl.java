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
package io.github.spleefx.v1_16_R3;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.spleefx.compatibility.ProtocolNMS;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.ExplosionSettings;
import net.minecraft.server.v1_16_R3.*;
import net.minecraft.server.v1_16_R3.Explosion.Effect;
import net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R3.LootTableInfo.Builder;
import net.minecraft.server.v1_16_R3.PacketPlayOutTitle.EnumTitleAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.*;

public class ProtocolNMSImpl implements ProtocolNMS {

    @Override
    public void displayTitle(Player player, String title, String subtitle, int fadeIn, int display, int fadeOut) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();
        PacketPlayOutTitle packetReset = new PacketPlayOutTitle(EnumTitleAction.RESET, null);
        p.playerConnection.sendPacket(packetReset);
        if (StringUtils.isNotEmpty(title)) {
            IChatBaseComponent chatTitle = ChatSerializer.a(String.format(TITLE_TEXT, Chat.colorize(title)));
            PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
            p.playerConnection.sendPacket(titlePacket);
        }
        if (StringUtils.isNotEmpty(subtitle)) {
            IChatBaseComponent chatSubtitle = ChatSerializer.a(String.format(TITLE_TEXT, Chat.colorize(subtitle)));
            PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, chatSubtitle);
            p.playerConnection.sendPacket(titlePacket);
        }
        PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, display, fadeOut);
        p.playerConnection.sendPacket(length);
    }

    @Override
    public void send(ComponentJSON component, CommandSender player) {
        EntityPlayer e = ((CraftPlayer) player).getHandle();
        PacketPlayOutChat c = new PacketPlayOutChat(ChatSerializer.a(component.toString()), ChatMessageType.CHAT, SystemUtils.b);
        e.playerConnection.sendPacket(c);
    }

    @Override
    public void displayActionBar(Player player, String text) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutChat
                (IChatBaseComponent.ChatSerializer.a(String.format(TITLE_TEXT, Chat.colorize(text))), ChatMessageType.GAME_INFO, SystemUtils.b));
    }

    @Override
    public void createExplosion(Location location, ExplosionSettings settings) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        WorldServer world = ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle();

        Explosion explosion = new Explosion(world, null, null, null,
                x, y, z, settings.getPower(), settings.breakBlocks(), Effect.BREAK);
        explosion.a();
        doExplosionB(location, settings, explosion, world);

        if (!explosion.wasCanceled) {
            if (!settings.breakBlocks())
                explosion.clearBlocks();

            for (EntityPlayer e : world.getPlayers()) {
                if (getDistanceSq(e.locX(), e.locY(), e.locZ(), x, y, z) < 4096.0D) {
                    e.playerConnection.sendPacket(new PacketPlayOutExplosion(x, y, z, settings.getPower(), explosion.getBlocks(),
                            explosion.c().get(e)));
                }
            }
        }
    }

    public void doExplosionB(Location location, ExplosionSettings settings, Explosion explosion, WorldServer world) {
        double posX = location.getX();
        double posY = location.getY();
        double posZ = location.getZ();
        if (world.isClientSide) {
            world.a(posX, posY, posZ, SoundEffects.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        if (settings.particles()) {
            if (settings.getPower() >= 2.0F) {
                world.addParticle(Particles.EXPLOSION_EMITTER, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
            } else {
                world.addParticle(Particles.EXPLOSION, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
            }
        }

        List<Pair<ItemStack, BlockPosition>> objectarraylist = new ArrayList<>();
        Collections.shuffle(explosion.getBlocks(), world.random);
        Iterator<BlockPosition> iterator;
        org.bukkit.World bworld = world.getWorld();
        List<org.bukkit.block.Block> blockList = Lists.newArrayList();

        for (int i1 = explosion.getBlocks().size() - 1; i1 >= 0; --i1) {
            BlockPosition cpos = explosion.getBlocks().get(i1);
            org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
            if (!bblock.getType().isAir()) {
                blockList.add(bblock);
            }
        }

        List<org.bukkit.block.Block> bukkitBlocks;
        float yield = settings.getYield();
        BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, 1.0F);
        world.getServer().getPluginManager().callEvent(event);
        bukkitBlocks = event.blockList();

        explosion.getBlocks().clear();

        for (org.bukkit.block.Block bukkitBlock : bukkitBlocks) {
            BlockPosition coords = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
            explosion.getBlocks().add(coords);
        }

        explosion.getBlocks().clear();

        for (org.bukkit.block.Block bblock : bukkitBlocks) {
            BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
            explosion.getBlocks().add(coords);
        }

        iterator = explosion.getBlocks().iterator();

        label111:
        while (true) {
            BlockPosition blockposition;
            IBlockData iblockdata;
            Block block;
            do {
                if (!iterator.hasNext()) {

                    for (Pair<ItemStack, BlockPosition> pair : objectarraylist) {
                        Block.a(world, pair.getSecond(), pair.getFirst());
                    }
                    break label111;
                }

                blockposition = iterator.next();
                iblockdata = world.getType(blockposition);
                block = iblockdata.getBlock();
            } while (iblockdata.isAir());

            BlockPosition blockposition1 = blockposition.immutableCopy();
            world.getMethodProfiler().enter("explosion_blocks");
            if (block.a(explosion)) {
                TileEntity tileentity = block.isTileEntity() ? world.getTileEntity(blockposition) : null;
                Builder loottableinfo_builder = (new Builder(world)).a(world.random).set(LootContextParameters.ORIGIN, Vec3D.a(blockposition)).set(LootContextParameters.TOOL, ItemStack.b).setOptional(LootContextParameters.BLOCK_ENTITY, tileentity).setOptional(LootContextParameters.THIS_ENTITY, null);
                if (yield < 1.0F) {
                    loottableinfo_builder.set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield);
                }

                iblockdata.a(loottableinfo_builder).forEach((itemstack) -> {
                    a(objectarraylist, itemstack, blockposition1);
                });
            }

            world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
            block.wasExploded(world, blockposition, explosion);
            world.getMethodProfiler().exit();
        }

        if (settings.createFire()) {

            for (BlockPosition blockposition2 : explosion.getBlocks()) {
                if (RANDOM.nextInt(3) == 0 && world.getType(blockposition2).isAir() && world.getType(blockposition2.down()).i(world, blockposition2.down()) && !CraftEventFactory.callBlockIgniteEvent(world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), explosion).isCancelled()) {
                    world.setTypeUpdate(blockposition2, BlockFireAbstract.a(((IBlockAccess) world), blockposition2));
                }
            }
        }
    }

    private static void a(List<Pair<ItemStack, BlockPosition>> explodedBlocks, ItemStack itemstack, BlockPosition blockposition) {
        int i = explodedBlocks.size();

        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPosition> pair = explodedBlocks.get(j);
            ItemStack itemstack1 = pair.getFirst();
            if (EntityItem.a(itemstack1, itemstack)) {
                try {
                    ItemStack itemstack2 = EntityItem.a(itemstack1, itemstack, 16);

                    explodedBlocks.set(j, Pair.of(itemstack2, pair.getSecond()));
                    if (itemstack.isEmpty()) {
                        return;
                    }
                } catch (AssertionError ignored) {
                }
            }
        }

        explodedBlocks.add(Pair.of(itemstack, blockposition));
    }
}

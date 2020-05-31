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
package io.github.spleefx.v1_14_R1;

import com.google.common.collect.Lists;
import io.github.spleefx.compatibility.ProtocolNMS;
import io.github.spleefx.compatibility.chat.ComponentJSON;
import io.github.spleefx.util.game.Chat;
import io.github.spleefx.util.game.ExplosionSettings;
import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_14_R1.Explosion.Effect;
import net.minecraft.server.v1_14_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_14_R1.LootTableInfo.Builder;
import net.minecraft.server.v1_14_R1.PacketPlayOutTitle.EnumTitleAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;
import java.util.List;

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
        PacketPlayOutChat c = new PacketPlayOutChat(ChatSerializer.a(component.toString()));
        e.playerConnection.sendPacket(c);
    }

    @Override
    public void createExplosion(Location location, ExplosionSettings settings) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        Explosion explosion = new Explosion(world, null, x, y, z, settings.getPower(), settings.createFire(), settings.breakBlocks() ? Effect.BREAK : Effect.NONE);
        explosion.a();
        doExplosionB(location, settings, explosion, world);

        if (!explosion.wasCanceled) {
            if (!settings.breakBlocks())
                explosion.clearBlocks();

            for (EntityPlayer e : world.getPlayers()) {
                if (getDistanceSq(e.locX, e.locY, e.locZ, x, y, z) < 4096.0D) {
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
        Entity source = null;
        Effect effect = settings.breakBlocks() ? Effect.BREAK : Effect.NONE;
        world.playSound(null, posX, posY, posZ, SoundEffects.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);
        boolean flag1 = effect != Effect.NONE;
        if (settings.getPower() >= 2.0F && flag1) {
            world.addParticle(Particles.EXPLOSION_EMITTER, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
        } else {
            world.addParticle(Particles.EXPLOSION, posX, posY, posZ, 1.0D, 0.0D, 0.0D);
        }

        Iterator<BlockPosition> iterator;
        BlockPosition blockposition;
        if (flag1) {
            org.bukkit.World bworld = world.getWorld();
            org.bukkit.entity.Entity explode = source == null ? null : source.getBukkitEntity();
            List<Block> blockList = Lists.newArrayList();

            for (int i1 = explosion.getBlocks().size() - 1; i1 >= 0; --i1) {
                BlockPosition cpos = explosion.getBlocks().get(i1);
                Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (bblock.getType() != org.bukkit.Material.AIR) {
                    blockList.add(bblock);
                }
            }

            boolean cancelled;
            List<Block> bukkitBlocks;
            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, effect == Effect.DESTROY ? settings.getYield() : 1.0F);
                world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, effect == Effect.DESTROY ? settings.getYield() : 1.0F);
                world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
            }

            explosion.getBlocks().clear();

            for (Block bukkitBlock : bukkitBlocks) {
                BlockPosition coords = new BlockPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
                explosion.getBlocks().add(coords);
            }

            if (cancelled) {
                explosion.wasCanceled = true;
                return;
            }

            iterator = explosion.getBlocks().iterator();

            label104:
            while (true) {
                IBlockData iblockdata;
                net.minecraft.server.v1_14_R1.Block block;
                do {
                    if (!iterator.hasNext()) {
                        break label104;
                    }

                    blockposition = iterator.next();
                    iblockdata = world.getType(blockposition);
                    block = iblockdata.getBlock();
                    if (settings.particles()) {
                        double d0 = (float) blockposition.getX() + world.random.nextFloat();
                        double d1 = (float) blockposition.getY() + world.random.nextFloat();
                        double d2 = (float) blockposition.getZ() + world.random.nextFloat();
                        double d3 = d0 - posX;
                        double d4 = d1 - posY;
                        double d5 = d2 - posZ;
                        double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                        d3 /= d6;
                        d4 /= d6;
                        d5 /= d6;
                        double d7 = 0.5D / (d6 / (double) settings.getPower() + 0.1D);
                        d7 *= world.random.nextFloat() * world.random.nextFloat() + 0.3F;
                        d3 *= d7;
                        d4 *= d7;
                        d5 *= d7;
                        world.addParticle(Particles.POOF, (d0 + posX) / 2.0D, (d1 + posY) / 2.0D, (d2 + posZ) / 2.0D, d3, d4, d5);
                        world.addParticle(Particles.SMOKE, d0, d1, d2, d3, d4, d5);
                    }
                } while (iblockdata.isAir());

                if (block.a(explosion) && world instanceof WorldServer) {
                    TileEntity tileentity = block.isTileEntity() ? world.getTileEntity(blockposition) : null;
                    Builder builder = (new Builder(world)).a(world.random).set(LootContextParameters.POSITION, blockposition).set(LootContextParameters.TOOL, ItemStack.a).setOptional(LootContextParameters.BLOCK_ENTITY, tileentity);
                    if (effect == Effect.DESTROY || settings.getYield() < 1.0F) {
                        builder.set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / settings.getYield());
                    }

                    net.minecraft.server.v1_14_R1.Block.b(iblockdata, builder);
                }

                world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
                block.wasExploded(world, blockposition, explosion);
            }
        }

        if (settings.createFire()) {
            iterator = explosion.getBlocks().iterator();

            while (iterator.hasNext()) {
                blockposition = iterator.next();
                if (world.getType(blockposition).isAir() && world.getType(blockposition.down()).g(world, blockposition.down()) && RANDOM.nextInt(3) == 0 && !CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), explosion).isCancelled()) {
                    world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                }
            }
        }

    }

}

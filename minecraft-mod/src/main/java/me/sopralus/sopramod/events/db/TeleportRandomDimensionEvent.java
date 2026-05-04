/*
 * Copyright (c) 2026 sopralus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.poc.sopramod.events.db;

import com.poc.sopramod.Sopramod;
import com.poc.sopramod.SopramodUtils;
import com.poc.sopramod.events.AbstractInstantEvent;
import com.poc.sopramod.events.EventType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Téléporte chaque joueur actif vers {@link Level#OVERWORLD}, {@link Level#NETHER} ou {@link Level#END},
 * en excluant toujours la dimension dans laquelle il se trouve déjà.
 */
public class TeleportRandomDimensionEvent extends AbstractInstantEvent {

    public static final EventType<TeleportRandomDimensionEvent> TYPE = EventType.builder(TeleportRandomDimensionEvent::new).build();

    int count = 0;

    @Override
    public void init() {
        Sopramod.getInstance().eventHandler.getActivePlayers().forEach(player -> {
            MinecraftServer server = player.level().getServer();
            if (server == null) {
                return;
            }
            ResourceKey<Level> current = player.level().dimension();
            List<ResourceKey<Level>> candidates = new ArrayList<>(3);
            for (ResourceKey<Level> key : List.of(Level.OVERWORLD, Level.NETHER, Level.END)) {
                if (!key.equals(current) && server.getLevel(key) != null) {
                    candidates.add(key);
                }
            }
            if (candidates.isEmpty()) {
                return;
            }
            ResourceKey<Level> targetKey = candidates.get(player.getRandom().nextInt(candidates.size()));
            ServerLevel target = server.getLevel(targetKey);
            Vec3 dest = pickDestination(target, server, player.getRandom());
            SopramodUtils.teleportPlayer(player, target, dest);
            SopramodUtils.clearPlayerArea(player);
        });
    }

    @Override
    public Component getDescription() {
        return Component.translatable("events.sopramod.teleport_random_dimension");
    }

    /**
     * {@return position debout plausible, sinon une valeur de secours}
     */
    private static Vec3 pickDestination(ServerLevel target, MinecraftServer server, RandomSource random) {
        if (target.dimension() == Level.END) {
            BlockPos endSpawn = ServerLevel.END_SPAWN_POINT;
            ensureChunkLoaded(target, endSpawn.getX(), endSpawn.getZ());
            return Vec3.atBottomCenterOf(endSpawn);
        }
        if (target.dimension() == Level.OVERWORLD) {
            BlockPos spawn = target.getRespawnData().pos();
            for (int attempt = 0; attempt < 40; attempt++) {
                int x = spawn.getX() + random.nextInt(129) - 64;
                int z = spawn.getZ() + random.nextInt(129) - 64;
                Vec3 v = surfaceAtOverworldLike(target, x, z);
                if (v != null) {
                    return v;
                }
            }
            Vec3 fb = surfaceAtOverworldLike(target, spawn.getX(), spawn.getZ());
            return fb != null ? fb : spawn.getBottomCenter();
        }

        ServerLevel overworld = server.overworld();
        BlockPos anchor = overworld.getRespawnData().pos();
        for (int attempt = 0; attempt < 48; attempt++) {
            int x = anchor.getX() / 8 + random.nextInt(81) - 40;
            int z = anchor.getZ() / 8 + random.nextInt(81) - 40;
            Vec3 v = netherStandingSpot(target, x, z);
            if (v != null) {
                return v;
            }
        }
        Vec3 fb = netherStandingSpot(target, anchor.getX() / 8, anchor.getZ() / 8);
        return fb != null ? fb : Vec3.atBottomCenterOf(new BlockPos(anchor.getX() / 8, 64, anchor.getZ() / 8));
    }

    private static void ensureChunkLoaded(ServerLevel level, int blockX, int blockZ) {
        ChunkPos cp = new ChunkPos(blockX >> 4, blockZ >> 4);
        level.getChunk(cp.x, cp.z, ChunkStatus.FULL, true);
    }

    private static Vec3 surfaceAtOverworldLike(ServerLevel level, int x, int z) {
        ensureChunkLoaded(level, x, z);
        BlockPos xz = new BlockPos(x, 0, z);
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xz);
        BlockPos feet = ground.above();
        if (feet.getY() >= level.getMaxY()) {
            return null;
        }
        if (!isSafeStand(level, feet)) {
            return null;
        }
        return Vec3.atBottomCenterOf(feet);
    }

    private static Vec3 netherStandingSpot(ServerLevel level, int x, int z) {
        ensureChunkLoaded(level, x, z);
        int minY = level.getMinY() + 4;
        int roof = Math.min(level.dimensionType().logicalHeight() - 8, level.getMaxY() - 4);
        for (int y = roof; y >= minY; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (!isSafeStand(level, feet)) {
                continue;
            }
            return Vec3.atBottomCenterOf(feet);
        }
        return null;
    }

    private static boolean isSafeStand(ServerLevel level, BlockPos feet) {
        BlockPos head = feet.above();
        if (!level.getBlockState(feet).canBeReplaced() || !level.getBlockState(head).canBeReplaced()) {
            return false;
        }
        var below = level.getBlockState(feet.below());
        if (!below.blocksMotion()) {
            return false;
        }
        return feet.getY() <= level.dimensionType().logicalHeight() - 8;
    }

    @Override
    public void tick() {
        if (count <= 2) {
            if (count == 2) {
                Sopramod.getInstance().eventHandler.getActivePlayers().forEach(SopramodUtils::clearPlayerArea);
            }
            count++;
        }
        super.tick();
    }

    @Override
    public void tickClient() {
        if (count <= 2) {
            count++;
        }
    }

    @Override
    public boolean hasEnded() {
        return count > 2;
    }

    @Override
    public EventType<TeleportRandomDimensionEvent> getType() {
        return TYPE;
    }
}

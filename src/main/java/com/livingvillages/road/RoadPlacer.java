package com.livingvillages.road;

import com.livingvillages.config.ModConfig;
import com.livingvillages.registry.VillageData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class RoadPlacer {
    private static final Random RANDOM = new Random();

    private BlockState getRoadMaterial(ServerLevel level) {
        return switch (ModConfig.get().road_material) {
            case "gravel" -> Blocks.GRAVEL.defaultBlockState();
            case "cobblestone" -> Blocks.COBBLESTONE.defaultBlockState();
            case "stone" -> Blocks.STONE.defaultBlockState();
            case "dirt" -> Blocks.DIRT_PATH.defaultBlockState();
            default -> Blocks.DIRT_PATH.defaultBlockState();
        };
    }

    private BlockState getPathMaterial(ServerLevel level) {
        return Blocks.DIRT_PATH.defaultBlockState();
    }

    public boolean tryPlaceRoad(VillageData village, ServerLevel level) {
        List<BlockPos> buildings = village.buildingPositions;
        if (buildings.size() < 2) return false;

        BlockPos center = village.center;
        BlockState roadMat = getRoadMaterial(level);
        boolean placed = false;

        int mainRoads = Math.min(2, buildings.size() / 3);
        for (int i = 0; i < mainRoads && i < buildings.size(); i++) {
            BlockPos building = buildings.get(i);
            if (RANDOM.nextInt(3) != 0) continue;
            if (layRoad(center, building, level, roadMat)) {
                placed = true;
            }
        }

        for (int i = 1; i < buildings.size(); i++) {
            if (RANDOM.nextInt(4) == 0) {
                if (layRoad(buildings.get(i - 1), buildings.get(i), level, getPathMaterial(level))) {
                    placed = true;
                }
            }
        }

        return placed;
    }

    private boolean layRoad(BlockPos from, BlockPos to, ServerLevel level, BlockState material) {
        int x1 = from.getX(), z1 = from.getZ();
        int x2 = to.getX(), z2 = to.getZ();
        int y = from.getY();

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;

        boolean placed = false;
        int maxSteps = 200;
        int step = 0;

        while (step < maxSteps) {
            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                z1 += sz;
            }

            BlockPos roadPos = findRoadSurface(level, new BlockPos(x1, y, z1));
            if (roadPos != null) {
                if (level.getBlockState(roadPos).isAir() || level.getBlockState(roadPos).getBlock() == Blocks.GRASS_BLOCK) {
                    level.setBlock(roadPos, material, 3);
                    for (int w = -1; w <= 1; w++) {
                        if (w == 0) continue;
                        BlockPos side = roadPos.offset(w, 0, 0);
                        if (level.getBlockState(side).getBlock() == Blocks.GRASS_BLOCK) {
                            level.setBlock(side, getPathMaterial(level), 3);
                        }
                        BlockPos sideZ = roadPos.offset(0, 0, w);
                        if (level.getBlockState(sideZ).getBlock() == Blocks.GRASS_BLOCK) {
                            level.setBlock(sideZ, getPathMaterial(level), 3);
                        }
                    }
                    placed = true;
                }
            }

            if (x1 == x2 && z1 == z2) break;
            step++;
        }
        return placed;
    }

    private BlockPos findRoadSurface(ServerLevel level, BlockPos pos) {
        int y = pos.getY();
        for (int i = -3; i <= 3; i++) {
            BlockPos check = pos.atY(y + i);
            BlockState state = level.getBlockState(check);
            if (!state.isAir() && state.blocksMotion() && level.getBlockState(check.above()).isAir()) {
                return check.above();
            }
        }
        return null;
    }
}

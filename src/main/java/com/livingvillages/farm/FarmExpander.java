package com.livingvillages.farm;

import com.livingvillages.registry.VillageData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.Random;

public class FarmExpander {
    private static final Random RANDOM = new Random();

    public boolean tryExpandFarm(VillageData village, ServerLevel level) {
        BlockPos farmCenter = findFarmSite(village, level);
        if (farmCenter == null) return false;
        return createFarmPlot(farmCenter, level);
    }

    public BlockPos findFarmSite(VillageData village, ServerLevel level) {
        BlockPos farmCenter = findExistingFarmland(village, level);
        if (farmCenter == null) {
            farmCenter = findSuitableFarmLocation(village, level);
        }
        return farmCenter;
    }

    public void placeFarmAt(BlockPos center, ServerLevel level) {
        createFarmPlot(center, level);
    }

    private BlockPos findExistingFarmland(VillageData village, ServerLevel level) {
        int radius = village.radius;
        for (int attempt = 0; attempt < 30; attempt++) {
            int dx = RANDOM.nextInt(radius * 2) - radius;
            int dz = RANDOM.nextInt(radius * 2) - radius;
            BlockPos check = village.center.offset(dx, 0, dz);
            check = findSurface(level, check);
            if (check == null) continue;

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos nearby = check.offset(x, -1, z);
                    if (level.getBlockState(nearby).getBlock() == Blocks.FARMLAND) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findSuitableFarmLocation(VillageData village, ServerLevel level) {
        int radius = village.radius;
        for (int attempt = 0; attempt < 20; attempt++) {
            int dx = RANDOM.nextInt(radius * 2) - radius;
            int dz = RANDOM.nextInt(radius * 2) - radius;

            if (Math.abs(dx) < 10 && Math.abs(dz) < 10) continue;

            BlockPos candidate = village.center.offset(dx, 0, dz);
            candidate = findSurface(level, candidate);
            if (candidate != null && isGrassArea(level, candidate, 7, 7)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isGrassArea(ServerLevel level, BlockPos center, int w, int d) {
        int halfW = w / 2;
        int halfD = d / 2;

        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfD; z <= halfD; z++) {
                BlockPos ground = center.offset(x, -1, z);
                BlockState state = level.getBlockState(ground);
                if (state.getBlock() != Blocks.GRASS_BLOCK && state.getBlock() != Blocks.DIRT) {
                    return false;
                }
                if (!level.getBlockState(ground.above()).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean createFarmPlot(BlockPos center, ServerLevel level) {
        int halfSize = 3;
        int waterX = center.getX();
        int waterZ = center.getZ();

        boolean placed = false;

        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                if (x == 0 && z == 0) {
                    level.setBlock(center.atY(center.getY() - 1), Blocks.WATER.defaultBlockState(), 3);
                    placed = true;
                    continue;
                }

                int dist = Math.abs(x) + Math.abs(z);
                if (dist > halfSize + 1) continue;

                BlockPos groundPos = center.offset(x, -1, z);
                BlockState ground = level.getBlockState(groundPos);

                if (dist <= halfSize && ground.getBlock() == Blocks.GRASS_BLOCK) {
                    level.setBlock(groundPos, Blocks.FARMLAND.defaultBlockState(), 3);
                    BlockPos cropPos = groundPos.above();
                    if (level.getBlockState(cropPos).isAir()) {
                        level.setBlock(cropPos, Blocks.WHEAT.defaultBlockState(), 3);
                        placed = true;
                    }
                }
            }
        }

        for (int x = -halfSize - 1; x <= halfSize + 1; x++) {
            for (int z = -halfSize - 1; z <= halfSize + 1; z++) {
                boolean edge = Math.abs(x) == halfSize + 1 || Math.abs(z) == halfSize + 1;
                if (edge) {
                    int dist = Math.abs(x) + Math.abs(z);
                    if (dist > halfSize + 2 && dist < halfSize + 4) {
                        BlockPos fencePos = center.offset(x, 0, z);
                        if (level.getBlockState(fencePos).isAir() || level.getBlockState(fencePos).getBlock() == Blocks.SHORT_GRASS) {
                            level.setBlock(fencePos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        return placed;
    }

    private BlockPos findSurface(ServerLevel level, BlockPos pos) {
        int y = level.getHeight();
        while (y > level.getMinY()) {
            BlockState state = level.getBlockState(pos.atY(y));
            if (!state.isAir() && state.blocksMotion()) {
                if (level.getBlockState(pos.atY(y + 1)).isAir()) {
                    return pos.atY(y + 1);
                }
            }
            y--;
        }
        return null;
    }
}

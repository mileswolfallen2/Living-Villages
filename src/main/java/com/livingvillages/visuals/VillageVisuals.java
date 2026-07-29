package com.livingvillages.visuals;

import com.livingvillages.config.ModConfig;
import com.livingvillages.registry.VillageData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.Random;

public class VillageVisuals {
    private static final Random RANDOM = new Random();

    public void apply(VillageData village, ServerLevel level) {
        ModConfig config = ModConfig.get();

        for (BlockPos building : village.buildingPositions) {
            if (config.place_lamps && RANDOM.nextInt(3) == 0) {
                placeLampNear(building, level);
            }
        }

        if (config.place_fences && village.buildCount >= 4) {
            placePerimeterFence(village, level);
        }

        if (config.place_banners && village.phase.getLevel() >= 2) {
            placeBanners(village, level);
        }
    }

    private void placeLampNear(BlockPos pos, ServerLevel level) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos lampPos = pos.relative(dir, 2);
            BlockPos surface = findSurface(level, lampPos);
            if (surface == null) continue;

            if (level.getBlockState(surface).isAir() && level.getBlockState(surface.below()).blocksMotion()) {
                level.setBlock(surface, Blocks.LANTERN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false), 3);
                return;
            }

            BlockPos hangingPos = pos.relative(dir, 1).above(2);
            if (level.getBlockState(hangingPos).isAir() && level.getBlockState(hangingPos.above()).blocksMotion()) {
                level.setBlock(hangingPos, Blocks.LANTERN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), 3);
                return;
            }
        }
    }

    private void placePerimeterFence(VillageData village, ServerLevel level) {
        int radius = Math.min(village.radius, 48);
        int segments = 8;

        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            int fx = (int) (village.center.getX() + radius * Math.cos(angle));
            int fz = (int) (village.center.getZ() + radius * Math.sin(angle));

            BlockPos fencePos = new BlockPos(fx, village.center.getY(), fz);
            BlockPos surface = findSurface(level, fencePos);
            if (surface != null && RANDOM.nextInt(4) == 0) {
                level.setBlock(surface, Blocks.OAK_FENCE.defaultBlockState(), 3);
            }
        }
    }

    private void placeBanners(VillageData village, ServerLevel level) {
        Direction[] dirs = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        for (Direction dir : dirs) {
            BlockPos bannerPos = village.center.relative(dir, 4);
            BlockPos surface = findSurface(level, bannerPos);
            if (surface == null) continue;

            BlockPos below = surface.below();
            if (level.getBlockState(below).blocksMotion() && level.getBlockState(surface).isAir()) {
                level.setBlock(below, Blocks.OAK_FENCE.defaultBlockState(), 3);
                level.setBlock(surface, Blocks.BANNER.white().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BannerBlock.ROTATION, dir.getOpposite().get2DDataValue() * 4), 3);
            }
        }
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

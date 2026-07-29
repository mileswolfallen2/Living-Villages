package com.livingvillages.building;

import com.livingvillages.building.BuildingPool.BlockLayer;
import com.livingvillages.building.BuildingPool.BuildingBlueprint;
import com.livingvillages.config.ModConfig;
import com.livingvillages.registry.VillageData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.Random;

public class BuildingPlacer {
    private static final Random RANDOM = new Random();

    public boolean tryPlaceBuilding(VillageData village, ServerLevel level) {
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos site = findBuildSite(village, level);
            if (site != null) {
                BuildingBlueprint blueprint = BuildingPool.selectBuilding(level, site);
                placeStructure(site, blueprint, level);
                village.buildingPositions.add(site);
                return true;
            }
        }
        return false;
    }

    private BlockPos findBuildSite(VillageData village, ServerLevel level) {
        int radius = village.radius;
        int attempts = 20;

        for (int i = 0; i < attempts; i++) {
            int dx = RANDOM.nextInt(radius * 2) - radius;
            int dz = RANDOM.nextInt(radius * 2) - radius;

            if (Math.abs(dx) < 8 && Math.abs(dz) < 8) continue;

            boolean tooClose = false;
            for (BlockPos bp : village.buildingPositions) {
                if (bp.distSqr(village.center.offset(dx, 0, dz)) < 25) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            BlockPos candidate = village.center.offset(dx, 0, dz);
            candidate = findSurface(level, candidate);
            if (candidate != null && isFlatEnough(level, candidate, 9, 7)) {
                return candidate;
            }
        }
        return null;
    }

    private BlockPos findSurface(ServerLevel level, BlockPos pos) {
        ChunkAccess chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) return null;

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

    private boolean isFlatEnough(ServerLevel level, BlockPos center, int w, int d) {
        int halfW = w / 2;
        int halfD = d / 2;
        int baseY = center.getY();

        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfD; z <= halfD; z++) {
                BlockPos check = center.offset(x, -1, z);
                BlockState state = level.getBlockState(check);
                if (state.isAir() || !state.blocksMotion()) return false;
                if (level.getBlockState(check.above()).blocksMotion()) return false;
            }
        }
        return true;
    }

    private void placeStructure(BlockPos origin, BuildingBlueprint blueprint, ServerLevel level) {
        Direction facing = Direction.from2DDataValue(RANDOM.nextInt(4));

        for (BlockLayer layer : blueprint.layers()) {
            int yOffset = layer.y();
            BlockState[][] pattern = layer.pattern();

            for (int x = 0; x < blueprint.width(); x++) {
                for (int z = 0; z < blueprint.depth(); z++) {
                    BlockPos target = rotatePos(origin, x, yOffset, z, blueprint.width(), blueprint.depth(), facing);
                    BlockState state = pattern[x][z];

                    if (state != null && !state.isAir()) {
                        if (state.getBlock() instanceof RotatedPillarBlock && facing.getAxis() != Direction.Axis.Y) {
                            state = state.trySetValue(RotatedPillarBlock.AXIS, facing.getAxis());
                        }
                        level.setBlock(target, state, 3);
                    }
                }
            }
        }

        if (blueprint.hasDoor()) {
            BlockPos doorPos = rotatePos(origin, blueprint.doorX(), 1, blueprint.doorZ(), blueprint.width(), blueprint.depth(), facing);
            BlockState doorLower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, facing)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.LOWER);
            level.setBlock(doorPos, doorLower, 3);
            BlockState doorUpper = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, facing)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, DoubleBlockHalf.UPPER);
            level.setBlock(doorPos.above(), doorUpper, 3);
        }
    }

    private BlockPos rotatePos(BlockPos origin, int x, int y, int z, int w, int d, Direction facing) {
        return switch (facing) {
            case NORTH -> origin.offset(x, y, z);
            case SOUTH -> origin.offset(w - 1 - x, y, d - 1 - z);
            case EAST -> origin.offset(z, y, w - 1 - x);
            case WEST -> origin.offset(d - 1 - z, y, x);
            default -> origin.offset(x, y, z);
        };
    }
}

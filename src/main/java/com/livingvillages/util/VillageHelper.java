package com.livingvillages.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.stream.Stream;

public class VillageHelper {

    public static Stream<BlockPos> findMeetingPoints(ServerLevel level, BlockPos center, int radius) {
        return level.getPoiManager().getInRange(
            poiType -> poiType.is(PoiTypes.MEETING),
            center,
            radius,
            PoiManager.Occupancy.ANY
        ).map(entry -> entry.getPos());
    }

    public static boolean isSurfaceFlat(ServerLevel level, BlockPos pos, int width, int depth) {
        int halfW = width / 2;
        int halfD = depth / 2;
        int baseY = pos.getY() - 1;

        for (int x = -halfW; x <= halfW; x++) {
            for (int z = -halfD; z <= halfD; z++) {
                BlockPos check = pos.offset(x, -1, z);
                BlockState state = level.getBlockState(check);
                if (state.isAir() || !state.blocksMotion()) return false;
                if (!level.getBlockState(check.above()).isAir()) return false;
            }
        }
        return true;
    }

    public static AABB villageBounds(BlockPos center, int radius) {
        return new AABB(
            center.getX() - radius, center.getY() - 32, center.getZ() - radius,
            center.getX() + radius, center.getY() + 32, center.getZ() + radius
        );
    }
}

package com.livingvillages.building;

import com.livingvillages.config.ModConfig;
import com.livingvillages.registry.VillageData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;
import java.util.Random;

public class StructurePlacer {
    private static final Random RANDOM = new Random();

    public record BuildPlan(BlockPos site, VillageStructurePool.StructureSelection selection) {}

    public BuildPlan findBuildSite(VillageData village, ServerLevel level) {
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos site = findLocation(village, level);
            if (site != null) {
                VillageStructurePool.StructureSelection sel = VillageStructurePool.selectStructure();
                return new BuildPlan(site, sel);
            }
        }
        return null;
    }

    public boolean placeStructure(BuildPlan plan, VillageData village, ServerLevel level) {
        StructureTemplateManager tm = level.getStructureManager();
        Optional<StructureTemplate> templateOpt = tm.get(plan.selection.id());
        if (templateOpt.isEmpty()) return false;

        StructureTemplate template = templateOpt.get();
        Rotation rotation = Rotation.values()[RANDOM.nextInt(Rotation.values().length)];

        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setIgnoreEntities(false)
            .setKnownShape(true);

        template.placeInWorld(level, plan.site, BlockPos.ZERO, settings, level.getRandom(), 3);
        return true;
    }

    private BlockPos findLocation(VillageData village, ServerLevel level) {
        int radius = village.radius;
        for (int i = 0; i < 20; i++) {
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
            if (candidate != null && isFlatEnough(level, candidate, 11, 11)) {
                return candidate;
            }
        }
        return null;
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
}

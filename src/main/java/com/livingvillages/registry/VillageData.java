package com.livingvillages.registry;

import com.livingvillages.evolution.VillagePhase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.List;

public class VillageData {
    private static final String TAG_CENTER = "Center";
    private static final String TAG_PHASE = "Phase";
    private static final String TAG_POPULATION = "Population";
    private static final String TAG_CREATION_TIME = "CreationTime";
    private static final String TAG_LAST_BUILD_TIME = "LastBuildTime";
    private static final String TAG_LAST_EVOLVE_TIME = "LastEvolveTime";
    private static final String TAG_BUILDINGS = "Buildings";
    private static final String TAG_RADIUS = "Radius";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_BUILD_COUNT = "BuildCount";

    public BlockPos center;
    public VillagePhase phase;
    public int population;
    public long creationTime;
    public long lastBuildTime;
    public long lastEvolveTime;
    public int radius;
    public ResourceKey<Level> dimension;
    public int buildCount;
    public List<BlockPos> buildingPositions;

    public VillageData(BlockPos center, ResourceKey<Level> dimension) {
        this.center = center;
        this.dimension = dimension;
        this.phase = VillagePhase.HAMLET;
        this.population = 0;
        this.creationTime = System.currentTimeMillis();
        this.lastBuildTime = 0;
        this.lastEvolveTime = 0;
        this.radius = 32;
        this.buildCount = 0;
        this.buildingPositions = new ArrayList<>();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        CompoundTag centerTag = new CompoundTag();
        centerTag.putInt("x", center.getX());
        centerTag.putInt("y", center.getY());
        centerTag.putInt("z", center.getZ());
        tag.put(TAG_CENTER, centerTag);
        tag.putString(TAG_PHASE, phase.getSerializedName());
        tag.putInt(TAG_POPULATION, population);
        tag.putLong(TAG_CREATION_TIME, creationTime);
        tag.putLong(TAG_LAST_BUILD_TIME, lastBuildTime);
        tag.putLong(TAG_LAST_EVOLVE_TIME, lastEvolveTime);
        tag.putInt(TAG_RADIUS, radius);
        tag.putString(TAG_DIMENSION, dimension.identifier().toString());
        tag.putInt(TAG_BUILD_COUNT, buildCount);

        ListTag buildingsList = new ListTag();
        for (BlockPos pos : buildingPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            buildingsList.add(posTag);
        }
        tag.put(TAG_BUILDINGS, buildingsList);

        return tag;
    }

    public static VillageData load(CompoundTag tag) {
        CompoundTag centerTag = tag.getCompoundOrEmpty(TAG_CENTER);
        BlockPos center = new BlockPos(centerTag.getIntOr("x", 0), centerTag.getIntOr("y", 0), centerTag.getIntOr("z", 0));
        VillagePhase phase = VillagePhase.fromName(tag.getStringOr(TAG_PHASE, "hamlet"));
        int population = tag.getIntOr(TAG_POPULATION, 0);
        long creationTime = tag.getLongOr(TAG_CREATION_TIME, 0);
        long lastBuildTime = tag.getLongOr(TAG_LAST_BUILD_TIME, 0);
        long lastEvolveTime = tag.getLongOr(TAG_LAST_EVOLVE_TIME, 0);
        int radius = tag.getIntOr(TAG_RADIUS, 32);
        Identifier dimId = Identifier.tryParse(tag.getStringOr(TAG_DIMENSION, "minecraft:overworld"));
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimId != null ? dimId : Identifier.withDefaultNamespace("overworld"));
        int buildCount = tag.getIntOr(TAG_BUILD_COUNT, 0);

        VillageData data = new VillageData(center, dimension);
        data.phase = phase;
        data.population = population;
        data.creationTime = creationTime;
        data.lastBuildTime = lastBuildTime;
        data.lastEvolveTime = lastEvolveTime;
        data.radius = radius;
        data.buildCount = buildCount;

        ListTag buildingsList = tag.getListOrEmpty(TAG_BUILDINGS);
        for (int i = 0; i < buildingsList.size(); i++) {
            CompoundTag posTag = buildingsList.getCompoundOrEmpty(i);
            data.buildingPositions.add(new BlockPos(posTag.getIntOr("x", 0), posTag.getIntOr("y", 0), posTag.getIntOr("z", 0)));
        }

        return data;
    }
}

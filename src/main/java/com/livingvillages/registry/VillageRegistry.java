package com.livingvillages.registry;

import com.livingvillages.LivingVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class VillageRegistry {
    private static final VillageRegistry INSTANCE = new VillageRegistry();
    private static final String DATA_FILE = "livingvillages.dat";

    private final Map<UUID, VillageData> villages = new HashMap<>();
    private final Map<BlockPos, UUID> centerIndex = new HashMap<>();
    private boolean loaded = false;

    public static VillageRegistry getInstance() {
        return INSTANCE;
    }

    public void loadAll(MinecraftServer server) {
        if (loaded) return;
        Path dataFile = server.getWorldPath(LevelResource.ROOT).resolve(DATA_FILE);
        if (dataFile.toFile().exists()) {
            try {
                CompoundTag tag = net.minecraft.nbt.NbtIo.read(dataFile);
                if (tag != null) {
                    ListTag villagesList = tag.getListOrEmpty("villages");
                    for (int i = 0; i < villagesList.size(); i++) {
                        CompoundTag villageTag = villagesList.getCompoundOrEmpty(i);
                        VillageData data = VillageData.load(villageTag);
                        UUID id = UUID.fromString(villageTag.getStringOr("UUID", ""));
                        villages.put(id, data);
                        centerIndex.put(data.center, id);
                    }
                    LivingVillages.LOGGER.info("Loaded {} villages", villages.size());
                }
            } catch (IOException e) {
                LivingVillages.LOGGER.error("Failed to load village data", e);
            }
        }

        loaded = true;
    }

    public void saveAll(MinecraftServer server) {
        Path dataFile = server.getWorldPath(LevelResource.ROOT).resolve(DATA_FILE);
        CompoundTag tag = new CompoundTag();
        ListTag villagesList = new ListTag();
        for (Map.Entry<UUID, VillageData> entry : villages.entrySet()) {
            CompoundTag villageTag = entry.getValue().save();
            villageTag.putString("UUID", entry.getKey().toString());
            villagesList.add(villageTag);
        }
        tag.put("villages", villagesList);
        try {
            java.nio.file.Files.createDirectories(dataFile.getParent());
            net.minecraft.nbt.NbtIo.write(tag, dataFile);
            LivingVillages.LOGGER.info("Saved {} villages", villages.size());
        } catch (IOException e) {
            LivingVillages.LOGGER.error("Failed to save village data", e);
        }
    }

    public void countPopulation(VillageData data, ServerLevel level) {
        AABB bounds = new AABB(
            data.center.getX() - data.radius, data.center.getY() - 16, data.center.getZ() - data.radius,
            data.center.getX() + data.radius, data.center.getY() + 16, data.center.getZ() + data.radius
        );
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, bounds);
        data.population = villagers.size();
    }

    public Optional<VillageData> getVillageAt(BlockPos pos) {
        UUID id = centerIndex.get(pos);
        if (id != null) return Optional.of(villages.get(id));

        for (VillageData data : villages.values()) {
            if (data.center.distSqr(pos) < data.radius * data.radius) {
                return Optional.of(data);
            }
        }
        return Optional.empty();
    }

    public Optional<VillageData> getVillage(BlockPos pos) {
        return getVillageAt(pos);
    }

    public Collection<VillageData> getAllVillages() {
        return villages.values();
    }

    public VillageData createVillage(BlockPos pos, ResourceKey<Level> dimension) {
        if (centerIndex.containsKey(pos)) return villages.get(centerIndex.get(pos));
        VillageData data = new VillageData(pos, dimension);
        UUID id = UUID.randomUUID();
        villages.put(id, data);
        centerIndex.put(pos, id);
        return data;
    }

    public void removeVillage(BlockPos pos) {
        UUID id = centerIndex.remove(pos);
        if (id != null) {
            villages.remove(id);
        }
    }

    public void updatePopulation(ServerLevel level) {
        for (VillageData data : villages.values()) {
            if (data.dimension == level.dimension()) {
                countPopulation(data, level);
            }
        }
    }
}

package com.livingvillages.evolution;

import com.livingvillages.building.BuildingPlacer;
import com.livingvillages.config.ModConfig;
import com.livingvillages.farm.FarmExpander;
import com.livingvillages.registry.VillageData;
import com.livingvillages.registry.VillageRegistry;
import com.livingvillages.road.RoadPlacer;
import com.livingvillages.visuals.VillageVisuals;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public class EvolutionScheduler {
    private final MinecraftServer server;
    private final BuildingPlacer buildingPlacer;
    private final RoadPlacer roadPlacer;
    private final FarmExpander farmExpander;
    private final VillageVisuals villageVisuals;
    private int tickCounter = 0;

    public EvolutionScheduler(MinecraftServer server) {
        this.server = server;
        this.buildingPlacer = new BuildingPlacer();
        this.roadPlacer = new RoadPlacer();
        this.farmExpander = new FarmExpander();
        this.villageVisuals = new VillageVisuals();
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        ModConfig config = ModConfig.get();

        int interval = (int) (config.build_cooldown_ticks / config.evolution_speed);
        if (interval < 100) interval = 100;

        if (tickCounter % 200 == 0) {
            for (ServerLevel level : server.getAllLevels()) {
                VillageRegistry.getInstance().updatePopulation(level);
            }
        }

        if (tickCounter % interval != 0) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (VillageData village : VillageRegistry.getInstance().getAllVillages()) {
                if (!village.dimension.equals(level.dimension())) continue;
                processVillage(village, level, config);
            }
        }
    }

    private void processVillage(VillageData village, ServerLevel level, ModConfig config) {
        long gameTime = level.getGameTime();
        boolean built = false;

        if (village.buildCount < config.max_buildings_per_village &&
            gameTime - village.lastBuildTime > config.build_cooldown_ticks) {
            if (buildingPlacer.tryPlaceBuilding(village, level)) {
                village.lastBuildTime = gameTime;
                village.buildCount++;
                built = true;
            }
        }

        if (gameTime - village.lastBuildTime > config.road_interval) {
            if (roadPlacer.tryPlaceRoad(village, level)) {
                built = true;
            }
        }

        if (gameTime - village.lastBuildTime > config.farm_expand_interval) {
            if (farmExpander.tryExpandFarm(village, level)) {
                built = true;
            }
        }

        if (village.buildCount >= 3) {
            villageVisuals.apply(village, level);
        }

        checkPhaseUpgrade(village);
    }

    private void checkPhaseUpgrade(VillageData village) {
        VillagePhase current = village.phase;
        VillagePhase next = current.next();
        if (next != current &&
            village.buildCount >= next.getMinBuildings() &&
            village.population >= next.getMinPopulation()) {
            village.phase = next;
            village.radius = Math.min(village.radius + 16, ModConfig.get().max_village_radius);
        }
    }
}

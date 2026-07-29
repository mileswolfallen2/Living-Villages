package com.livingvillages.evolution;

import com.livingvillages.building.BuildingPlacer;
import com.livingvillages.building.StructurePlacer;
import com.livingvillages.building.StructurePlacer.BuildPlan;
import com.livingvillages.config.ModConfig;
import com.livingvillages.construction.ConstructionManager;
import com.livingvillages.construction.ConstructionProject;
import com.livingvillages.farm.FarmExpander;
import com.livingvillages.registry.VillageData;
import com.livingvillages.registry.VillageRegistry;
import com.livingvillages.road.RoadPlacer;
import com.livingvillages.road.RoadPlacer.RoadSegment;
import com.livingvillages.visuals.VillageVisuals;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

public class EvolutionScheduler {
    private static final Random RANDOM = new Random();
    private final MinecraftServer server;
    private final BuildingPlacer buildingPlacer;
    private final StructurePlacer structurePlacer;
    private final RoadPlacer roadPlacer;
    private final FarmExpander farmExpander;
    private final VillageVisuals villageVisuals;
    private final ConstructionManager constructionManager;
    private int tickCounter = 0;

    public EvolutionScheduler(MinecraftServer server, ConstructionManager constructionManager) {
        this.server = server;
        this.constructionManager = constructionManager;
        this.buildingPlacer = new BuildingPlacer();
        this.structurePlacer = new StructurePlacer();
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

        if (village.buildCount < config.max_buildings_per_village &&
            gameTime - village.lastBuildTime > config.build_cooldown_ticks) {

            StructurePlacer.BuildPlan plan = structurePlacer.findBuildSite(village, level);
            if (plan != null) {
                BlockPos capturedSite = plan.site().immutable();
                int workTicks = plan.selection().workTicks();
                ConstructionProject project = new ConstructionProject(
                    ConstructionProject.Type.BUILDING,
                    capturedSite,
                    village,
                    workTicks,
                    () -> {
                        boolean placed = structurePlacer.placeStructure(plan, village, level);
                        if (placed) {
                            village.buildingPositions.add(capturedSite);
                            village.lastBuildTime = level.getGameTime();
                            village.buildCount++;
                        }
                    }
                );
                constructionManager.addProject(project);
            }
        }

        if (gameTime - village.lastBuildTime > config.road_interval) {
            List<RoadSegment> roads = roadPlacer.findRoadTargets(village, level);
            for (RoadSegment road : roads) {
                BlockPos from = road.from().immutable();
                BlockPos to = road.to().immutable();
                ConstructionProject project = new ConstructionProject(
                    ConstructionProject.Type.ROAD,
                    to,
                    village,
                    40,
                    () -> roadPlacer.placeRoadSegment(new RoadSegment(from, to, road.material()), level)
                );
                constructionManager.addProject(project);
            }
        }

        if (gameTime - village.lastBuildTime > config.farm_expand_interval) {
            BlockPos farmSite = farmExpander.findFarmSite(village, level);
            if (farmSite != null) {
                BlockPos capturedSite = farmSite.immutable();
                ConstructionProject project = new ConstructionProject(
                    ConstructionProject.Type.FARM,
                    capturedSite,
                    village,
                    60,
                    () -> farmExpander.placeFarmAt(capturedSite, level)
                );
                constructionManager.addProject(project);
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

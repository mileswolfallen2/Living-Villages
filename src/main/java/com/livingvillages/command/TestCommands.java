package com.livingvillages.command;

import com.livingvillages.LivingVillages;
import com.livingvillages.building.BuildingPlacer;
import com.livingvillages.building.StructurePlacer;
import com.livingvillages.building.StructurePlacer.BuildPlan;
import com.livingvillages.config.ModConfig;
import com.livingvillages.construction.ConstructionManager;
import com.livingvillages.construction.ConstructionProject;
import com.livingvillages.evolution.VillagePhase;
import com.livingvillages.farm.FarmExpander;
import com.livingvillages.guardian.GuardianSystem;
import com.livingvillages.registry.VillageData;
import com.livingvillages.registry.VillageRegistry;
import com.livingvillages.road.RoadPlacer;
import com.livingvillages.road.RoadPlacer.RoadSegment;
import com.livingvillages.visuals.VillageVisuals;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;

public class TestCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("livingvillages")
            .then(Commands.literal("test")
                .then(Commands.literal("phase")
                    .then(Commands.argument("phase", StringArgumentType.word())
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();
                            ServerLevel level = src.getLevel();
                            BlockPos pos = BlockPos.containing(src.getPosition());
                            String phaseName = StringArgumentType.getString(ctx, "phase");

                            VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                            if (village == null) {
                                village = VillageRegistry.getInstance().createVillage(pos, level.dimension());
                                src.sendSuccess(() -> Component.literal("Created new village at " + pos), true);
                            }

                            VillagePhase phase = VillagePhase.fromName(phaseName);
                            village.phase = phase;
                            src.sendSuccess(() -> Component.literal("Set village phase to " + phase.getSerializedName()), true);
                            return Command.SINGLE_SUCCESS;
                        }))
                )

                .then(Commands.literal("build")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                        if (village == null) {
                            src.sendFailure(Component.literal("No village found at your position"));
                            return 0;
                        }

                        StructurePlacer sp = new StructurePlacer();
                        BuildPlan plan = sp.findBuildSite(village, level);
                        if (plan == null) {
                            src.sendFailure(Component.literal("Could not find suitable build site"));
                            return 0;
                        }

                        ConstructionManager cm = LivingVillages.getInstance().getConstructionManager();
                        BlockPos capturedSite = plan.site().immutable();
                        int workTicks = plan.selection().workTicks();
                        String size = plan.selection().size();
                        ConstructionProject project = new ConstructionProject(
                            ConstructionProject.Type.BUILDING,
                            capturedSite,
                            village,
                            workTicks,
                            () -> {
                                if (sp.placeStructure(plan, village, level)) {
                                    village.buildingPositions.add(capturedSite);
                                    village.buildCount++;
                                }
                            }
                        );
                        cm.addProject(project);
                        src.sendSuccess(() -> Component.literal("Queued " + size + " house at " + capturedSite + " (~" + (workTicks / 20) + "s work)"), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("road")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                        if (village == null) {
                            src.sendFailure(Component.literal("No village found at your position"));
                            return 0;
                        }

                        RoadPlacer placer = new RoadPlacer();
                        RoadPlacer.RoadSegment segment = placer.findRoadTargets(village, level).stream().findFirst().orElse(null);
                        if (segment == null) {
                            src.sendFailure(Component.literal("Could not find road target"));
                            return 0;
                        }

                        ConstructionManager cm = LivingVillages.getInstance().getConstructionManager();
                        BlockPos to = segment.to().immutable();
                        ConstructionProject project = new ConstructionProject(
                            ConstructionProject.Type.ROAD,
                            to,
                            village,
                            40,
                            () -> placer.placeRoadSegment(segment, level)
                        );
                        cm.addProject(project);
                        src.sendSuccess(() -> Component.literal("Road project queued — villager will build it"), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("farm")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                        if (village == null) {
                            src.sendFailure(Component.literal("No village found at your position"));
                            return 0;
                        }

                        FarmExpander expander = new FarmExpander();
                        BlockPos site = expander.findFarmSite(village, level);
                        if (site == null) {
                            src.sendFailure(Component.literal("Could not find farm site"));
                            return 0;
                        }

                        ConstructionManager cm = LivingVillages.getInstance().getConstructionManager();
                        BlockPos capturedSite = site.immutable();
                        ConstructionProject project = new ConstructionProject(
                            ConstructionProject.Type.FARM,
                            capturedSite,
                            village,
                            60,
                            () -> expander.placeFarmAt(capturedSite, level)
                        );
                        cm.addProject(project);
                        src.sendSuccess(() -> Component.literal("Farm project queued at " + capturedSite + " — villager will build it"), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("projects")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        ConstructionManager cm = LivingVillages.getInstance().getConstructionManager();
                        var projects = cm.getActiveProjects();
                        if (projects.isEmpty()) {
                            src.sendSuccess(() -> Component.literal("No active construction projects"), false);
                        } else {
                            src.sendSuccess(() -> Component.literal("=== Construction Projects ==="), false);
                            for (ConstructionProject p : projects) {
                                String status = switch (p.state) {
                                    case QUEUED -> "QUEUED";
                                    case IN_PROGRESS -> "IN PROGRESS (villager: " + (p.assignedVillager != null ? p.assignedVillager.toString().substring(0, 8) : "none") + ")";
                                    case COMPLETE -> "COMPLETE";
                                };
                                src.sendSuccess(() -> Component.literal(
                                    "  [" + p.type + "] " + p.site.toShortString() +
                                    " | " + status +
                                    " | " + p.workRemaining + "/" + p.totalWork + " ticks remaining"
                                ), false);
                            }
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("patrol")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                        if (village == null) {
                            src.sendFailure(Component.literal("No village found at your position"));
                            return 0;
                        }

                        GuardianSystem guards = new GuardianSystem(level.getServer());
                        int count = guards.spawnGuardDebug(village, level);
                        src.sendSuccess(() -> Component.literal("Spawned " + count + " guard(s) at village"), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("visual")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                        if (village == null) {
                            src.sendFailure(Component.literal("No village found at your position"));
                            return 0;
                        }

                        VillageVisuals visuals = new VillageVisuals();
                        visuals.apply(village, level);
                        src.sendSuccess(() -> Component.literal("Visuals applied to village"), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("scan")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();

                        VillageRegistry.getInstance().updatePopulation(level);
                        int count = (int) VillageRegistry.getInstance().getAllVillages()
                            .stream().filter(v -> v.dimension.equals(level.dimension())).count();
                        src.sendSuccess(() -> Component.literal("Scanned villages. Found " + count + " in this dimension"), true);
                        return Command.SINGLE_SUCCESS;
                    })
                )

                .then(Commands.literal("generate")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        ServerLevel level = src.getLevel();
                        BlockPos pos = BlockPos.containing(src.getPosition());

                        VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                        boolean isNew = village == null;
                        if (isNew) {
                            village = VillageRegistry.getInstance().createVillage(pos, level.dimension());
                        }
                        final VillageData fv = village;
                        if (isNew) {
                            src.sendSuccess(() -> Component.literal("Created new village at " + pos), true);
                        }

                        fv.phase = VillagePhase.TOWN;
                        fv.radius = 48;

                        int[] villagersSpawned = {0};
                        for (int i = 0; i < 5; i++) {
                            BlockPos spawnPos = fv.center.offset(
                                (i - 2) * 2, 0, (i % 3) * 2 - 2);
                            int y = level.getHeight();
                            while (y > level.getMinY() && !level.getBlockState(spawnPos.atY(y)).blocksMotion()) {
                                y--;
                            }
                            BlockPos ground = spawnPos.atY(y + 1);

                            Villager villager = EntityTypes.VILLAGER.create(level, EntitySpawnReason.EVENT);
                            if (villager != null) {
                                villager.setPos(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5);
                                level.addFreshEntity(villager);
                                villagersSpawned[0]++;
                            }
                        }
                        VillageRegistry.getInstance().updatePopulation(level);

                        StructurePlacer sp = new StructurePlacer();
                        ConstructionManager cm = LivingVillages.getInstance().getConstructionManager();

                        int[] buildingsQueued = {0};
                        for (int i = 0; i < 3; i++) {
                            BuildPlan plan = sp.findBuildSite(fv, level);
                            if (plan != null) {
                                BlockPos captured = plan.site().immutable();
                                int workTicks = plan.selection().workTicks();
                                cm.addProject(new ConstructionProject(
                                    ConstructionProject.Type.BUILDING, captured, fv, workTicks,
                                    () -> {
                                        if (sp.placeStructure(plan, fv, level)) {
                                            fv.buildingPositions.add(captured);
                                            fv.buildCount++;
                                        }
                                    }));
                                buildingsQueued[0]++;
                            }
                        }

                        RoadPlacer rp = new RoadPlacer();
                        int[] roadsQueued = {0};
                        for (RoadSegment seg : rp.findRoadTargets(fv, level)) {
                            BlockPos to = seg.to().immutable();
                            cm.addProject(new ConstructionProject(
                                ConstructionProject.Type.ROAD, to, fv, 40,
                                () -> rp.placeRoadSegment(seg, level)));
                            roadsQueued[0]++;
                        }

                        FarmExpander fe = new FarmExpander();
                        int[] farmsQueued = {0};
                        BlockPos farmSite = fe.findFarmSite(fv, level);
                        if (farmSite != null) {
                            BlockPos captured = farmSite.immutable();
                            cm.addProject(new ConstructionProject(
                                ConstructionProject.Type.FARM, captured, fv, 60,
                                () -> fe.placeFarmAt(captured, level)));
                            farmsQueued[0]++;
                        }

                        int vs = villagersSpawned[0], bq = buildingsQueued[0], rq = roadsQueued[0], fq = farmsQueued[0];
                        src.sendSuccess(() -> Component.literal("=== Village Generated ==="), false);
                        src.sendSuccess(() -> Component.literal("Center: " + fv.center.toShortString()), false);
                        src.sendSuccess(() -> Component.literal("Phase: " + fv.phase.getSerializedName()), false);
                        src.sendSuccess(() -> Component.literal("Spawned " + vs + " villagers (name tags show jobs)"), false);
                        src.sendSuccess(() -> Component.literal("Queued " + bq + " buildings, " + rq + " roads, " + fq + " farms"), false);
                        src.sendSuccess(() -> Component.literal("Villagers will build everything — stand back and watch!"), false);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )

            .then(Commands.literal("list")
                .executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    var villages = VillageRegistry.getInstance().getAllVillages();
                    if (villages.isEmpty()) {
                        src.sendSuccess(() -> Component.literal("No villages registered"), false);
                    } else {
                        src.sendSuccess(() -> Component.literal("=== Villages ==="), false);
                        for (VillageData v : villages) {
                            src.sendSuccess(() -> Component.literal(
                                "  " + v.center.toShortString() +
                                " | Phase: " + v.phase.getSerializedName() +
                                " | Pop: " + v.population +
                                " | Buildings: " + v.buildingPositions.size() +
                                " | Radius: " + v.radius
                            ), false);
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
            )

            .then(Commands.literal("status")
                .executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    BlockPos pos = BlockPos.containing(src.getPosition());

                    VillageData village = VillageRegistry.getInstance().getVillage(pos).orElse(null);
                    if (village == null) {
                        src.sendFailure(Component.literal("No village at your position"));
                        return 0;
                    }

                    src.sendSuccess(() -> Component.literal("=== Village Status ==="), false);
                    src.sendSuccess(() -> Component.literal("Center: " + village.center.toShortString()), false);
                    src.sendSuccess(() -> Component.literal("Phase: " + village.phase.getSerializedName()), false);
                    src.sendSuccess(() -> Component.literal("Population: " + village.population), false);
                    src.sendSuccess(() -> Component.literal("Buildings: " + village.buildingPositions.size()), false);
                    src.sendSuccess(() -> Component.literal("Radius: " + village.radius), false);
                    src.sendSuccess(() -> Component.literal("Dimension: " + village.dimension.identifier()), false);
                    return Command.SINGLE_SUCCESS;
                })
            )

            .then(Commands.literal("reload")
                .executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ModConfig.reload();
                    src.sendSuccess(() -> Component.literal("Config reloaded"), true);
                    return Command.SINGLE_SUCCESS;
                })
            )
        );
    }
}

package com.livingvillages.command;

import com.livingvillages.building.BuildingPlacer;
import com.livingvillages.config.ModConfig;
import com.livingvillages.evolution.VillagePhase;
import com.livingvillages.farm.FarmExpander;
import com.livingvillages.guardian.GuardianSystem;
import com.livingvillages.registry.VillageData;
import com.livingvillages.registry.VillageRegistry;
import com.livingvillages.road.RoadPlacer;
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

                        BuildingPlacer placer = new BuildingPlacer();
                        boolean result = placer.tryPlaceBuilding(village, level);
                        if (result) {
                            src.sendSuccess(() -> Component.literal("Building placed at village"), true);
                        } else {
                            src.sendFailure(Component.literal("Could not find suitable build site"));
                        }
                        return result ? Command.SINGLE_SUCCESS : 0;
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
                        boolean result = placer.tryPlaceRoad(village, level);
                        if (result) {
                            src.sendSuccess(() -> Component.literal("Roads placed at village"), true);
                        } else {
                            src.sendFailure(Component.literal("Could not place roads"));
                        }
                        return result ? Command.SINGLE_SUCCESS : 0;
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
                        boolean result = expander.tryExpandFarm(village, level);
                        if (result) {
                            src.sendSuccess(() -> Component.literal("Farm expanded at village"), true);
                        } else {
                            src.sendFailure(Component.literal("Could not expand farm"));
                        }
                        return result ? Command.SINGLE_SUCCESS : 0;
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
